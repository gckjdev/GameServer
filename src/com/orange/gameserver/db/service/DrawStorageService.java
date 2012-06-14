package com.orange.gameserver.db.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.RandomUtil;
import com.orange.game.constants.DBConstants;
import com.orange.gameserver.db.DrawDBClient;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.utils.GameLog;

public class DrawStorageService {

	
	protected static final int MIN_DRAW_DATA_LEN = 3000;
	protected static final int EXECUTOR_POOL_NUM = 5;

	CopyOnWriteArrayList<ExecutorService> executorList = new CopyOnWriteArrayList<ExecutorService>();
	
	// thread-safe singleton implementation
    private static DrawStorageService manager = new DrawStorageService();     
    private DrawStorageService(){		
    	for (int i=0; i<EXECUTOR_POOL_NUM; i++){
    		ExecutorService executor = Executors.newFixedThreadPool(1);
    		executorList.add(executor);
    	}
	} 	    
    public static DrawStorageService getInstance() { 
    	return manager; 
    }
	
    
    public void executeDB(final int sessionId, Runnable runnable){
    	ExecutorService executor = getExecutor(sessionId);
    	executor.execute(runnable);    	
    }
    
    private ExecutorService getExecutor(int sessionId) {
    	int index = sessionId % EXECUTOR_POOL_NUM;    	
		return executorList.get(index);
	}
    
    public void storeDraw(final int sessionId, final User user, final String wordText, final int wordLevel,
    		final int language, final byte[] data) {
		
    	ExecutorService executor = getExecutor(sessionId);
    	
    	executor.execute(new Runnable(){

			@Override
			public void run() {
				
				if (data.length < MIN_DRAW_DATA_LEN){
					GameLog.info(sessionId, "save data into DB, but data too short, data bytes="+data.length);
					return;
				}
				
				// insert data into mongo DB here
				MongoDBClient dbClient = DrawDBClient.getInstance().getMongoClient();
				BasicDBObject docObject = new BasicDBObject();
				docObject.put(DBConstants.F_DRAW_USER_ID, user.getUserId());
				docObject.put(DBConstants.F_DRAW_NICK_NAME, user.getNickName());
				docObject.put(DBConstants.F_DRAW_AVATAR, user.getAvatar());
				docObject.put(DBConstants.F_DRAW_WORD, wordText);
				docObject.put(DBConstants.F_DRAW_LEVEL, wordLevel);
				docObject.put(DBConstants.F_DRAW_LANGUAGE, language);
				docObject.put(DBConstants.F_DRAW_CREATE_DATE, new Date());
				docObject.put(DBConstants.F_DRAW_DATA, data);
				docObject.put(DBConstants.F_DRAW_DATA_LEN, data.length);
				docObject.put(DBConstants.F_DRAW_RANDOM, Math.random());
				BasicDBList list = new BasicDBList();
				list.add(user.getUserId());
				docObject.put(DBConstants.F_DRAW_VIEW_USER_LIST, list);
				
				dbClient.insert(DBConstants.T_DRAW, docObject);
				
				GameLog.info(sessionId, "save data into DB, data bytes="+data.length);
			}
    	
    	});
	} 
  
	public BasicDBObject randomGetDraw(int sessionId, int language, Set<String> excludeUserIdList){
    	double rand = Math.random();
    	MongoDBClient dbClient = DrawDBClient.getInstance().getMongoClient();
    	BasicDBObject query = new BasicDBObject();
    	BasicDBObject gte = new BasicDBObject();
    	gte.put("$gte", rand);
    	query.put(DBConstants.F_DRAW_RANDOM, gte);
    	query.put(DBConstants.F_DRAW_LANGUAGE, language);
    	
    	BasicDBObject notInCondition = new BasicDBObject();
    	BasicDBList excludeList = new BasicDBList();
    	excludeList.addAll(excludeUserIdList);
    	notInCondition.put("$nin", excludeList);
    	query.put(DBConstants.F_DRAW_VIEW_USER_LIST, notInCondition);
    	    	
    	BasicDBObject update = new BasicDBObject();
    	BasicDBObject pushValue = new BasicDBObject();
    	BasicDBList pushList = new BasicDBList();
    	pushList.addAll(excludeUserIdList);
    	
    	BasicDBObject eachValue = new BasicDBObject();
    	eachValue.put("$each", pushList);
    	
    	pushValue.put(DBConstants.F_DRAW_VIEW_USER_LIST, eachValue);
    	update.put("$addToSet", pushValue);
    	
    	GameLog.info(sessionId, "<randomGetDraw> query = "+query.toString());
    	DBObject obj = dbClient.findOne(DBConstants.T_DRAW, query);

    	if (obj == null){
    		// try random from lte
    		BasicDBObject lte = new BasicDBObject();
    		lte.put("lte", rand);
    		query.clear();
    		query.put(DBConstants.F_DRAW_RANDOM, lte);
        	query.put(DBConstants.F_DRAW_LANGUAGE, language);    		

        	notInCondition = new BasicDBObject();
        	notInCondition.put("$nin", excludeList);
        	query.put(DBConstants.F_DRAW_VIEW_USER_LIST, notInCondition);
    		
        	GameLog.info(sessionId, "<randomGetDraw> query = "+query.toString());
    		obj = dbClient.findOne(DBConstants.T_DRAW, query);
    		
    	}
    	
		if (obj != null){
			BasicDBObject keyQuery = new BasicDBObject();
			keyQuery.put("_id", obj.get("_id"));
			GameLog.info(sessionId, "<randomGetDraw> update = "+keyQuery.toString() + ", update="+update.toString());
			dbClient.findAndModify(DBConstants.T_DRAW, keyQuery, update);
		}

		if (obj == null){
    		GameLog.info(sessionId, "random fetch data from DB, but no record return");
    		return null;
    	}
    	
    	byte[] data = (byte[])obj.get(DBConstants.F_DRAW_DATA);
    	if (data == null){
    		GameLog.info(sessionId, "random fetch data from DB, but draw data null?");
    		return null;    		
    	}
    	
    	GameLog.info(sessionId, "random fetch data from DB, data bytes="+data.length);
    	return (BasicDBObject)obj;
    }
    
	public void storeGuessWord(final int sessionId, final String word, final int language,
			final Collection<String> collection) {
		
    	ExecutorService executor = getExecutor(sessionId);
		
		executor.execute(new Runnable(){

			@Override
			public void run() {
				
				if (word == null || collection == null || collection.size() == 0)
					return;
				
				String wordText = word.toUpperCase();
				
				// insert data into mongo DB here
				MongoDBClient dbClient = DrawDBClient.getInstance().getMongoClient();
				
				BasicDBObject query = new BasicDBObject();
				query.put(DBConstants.F_DRAW_WORD, wordText);
				
				BasicDBList eachValueList = new BasicDBList();
				eachValueList.addAll(collection);
				
				BasicDBObject eachValue = new BasicDBObject();
		    	eachValue.put("$each", eachValueList);
		    	
		    	BasicDBObject addToSetValue = new BasicDBObject (); 
		    	addToSetValue.put(DBConstants.F_GUESS_WORD_LIST, eachValue);
		    	
		    	BasicDBObject setValue = new BasicDBObject();
		    	setValue.put(DBConstants.F_DRAW_WORD, wordText);
		    	
		    	BasicDBObject update = new BasicDBObject ();
		    	update.put("$addToSet", addToSetValue);
		    	update.put("$set", setValue);
		    					
		    	GameLog.info(sessionId, "<storeGuessWord> query="+query.toString()+", update="+update.toString());
				GameLog.info(sessionId, "save guess word data into DB, word count ="+collection.size());
				dbClient.updateOrInsert(DBConstants.T_GUESS, query, update);				
			}
    	
    	});
		
	}
	
	public String randomGetWord(int language, String word) {
		
		if (word == null){
			return null;
		}
		
		// insert data into mongo DB here
		MongoDBClient dbClient = DrawDBClient.getInstance().getMongoClient();
		
		BasicDBObject query = new BasicDBObject();
		query.put(DBConstants.F_DRAW_WORD, word.toUpperCase());
				
		DBObject obj = dbClient.findOne(DBConstants.T_GUESS, query);
		if (obj == null)
			return null;
		
		BasicDBList list = (BasicDBList)obj.get(DBConstants.F_GUESS_WORD_LIST);
		if (list == null || list.size() == 0)
			return null;
		
		/*
		int wordLen = word.length();
		List<String> candidateSet = new ArrayList<String>();
		for (Object candidate : list){
			String s = (String)candidate;
			if (s.length() == wordLen){
				candidateSet.add(s);
			}
		}
		
		if (candidateSet.size() == 0)
			return null;
		*/
		
		List<Object> candidateSet = list; 
		
		int randomIndex = RandomUtil.random(candidateSet.size()); 		
		return (String)candidateSet.get(randomIndex);
	}
    
}
