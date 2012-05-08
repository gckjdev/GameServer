package com.orange.gameserver.db.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.common.utils.RandomUtil;
import com.orange.gameserver.db.DrawDBClient;
import com.orange.gameserver.draw.utils.GameLog;

public class DrawStorageService {

	
	protected static final int MIN_DRAW_DATA_LEN = 1000;
	// thread-safe singleton implementation
    private static DrawStorageService manager = new DrawStorageService();     
    private DrawStorageService(){		
	} 	    
    public static DrawStorageService getInstance() { 
    	return manager; 
    }
	
    ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public void storeDraw(final int sessionId, final String drawUserId, final String wordText, final int wordLevel,
    		final int language, final byte[] data) {
		
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
				docObject.put(DrawDBClient.F_USER_ID, drawUserId);
				docObject.put(DrawDBClient.F_WORD, wordText);
				docObject.put(DrawDBClient.F_LEVEL, wordLevel);
				docObject.put(DrawDBClient.F_LANGUAGE, language);
				docObject.put(DrawDBClient.F_CREATE_DATE, new Date());
				docObject.put(DrawDBClient.F_DRAW_DATA, data);
				docObject.put(DrawDBClient.F_RANDOM, Math.random());
				BasicDBList list = new BasicDBList();
				list.add(drawUserId);
				docObject.put(DrawDBClient.F_VIEW_USER_LIST, list);
				
				dbClient.insert(DrawDBClient.T_DRAW, docObject);
				
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
    	query.put(DrawDBClient.F_RANDOM, gte);
    	query.put(DrawDBClient.F_LANGUAGE, language);
    	
    	BasicDBObject notInCondition = new BasicDBObject();
    	BasicDBList excludeList = new BasicDBList();
    	excludeList.addAll(excludeUserIdList);
    	notInCondition.put("$nin", excludeList);
    	query.put(DrawDBClient.F_VIEW_USER_LIST, notInCondition);
    	    	
    	BasicDBObject update = new BasicDBObject();
    	BasicDBObject pushValue = new BasicDBObject();
    	BasicDBList pushList = new BasicDBList();
    	pushList.addAll(excludeUserIdList);
    	
    	BasicDBObject eachValue = new BasicDBObject();
    	eachValue.put("$each", pushList);
    	
    	pushValue.put(DrawDBClient.F_VIEW_USER_LIST, eachValue);
    	update.put("$addToSet", pushValue);
    	
    	GameLog.info(sessionId, "<randomGetDraw> query = "+query.toString());
    	DBObject obj = dbClient.findOne(DrawDBClient.T_DRAW, query);

    	if (obj == null){
    		// try random from lte
    		BasicDBObject lte = new BasicDBObject();
    		lte.put("lte", rand);
    		query.clear();
    		query.put(DrawDBClient.F_RANDOM, lte);
        	query.put(DrawDBClient.F_LANGUAGE, language);    		

        	notInCondition = new BasicDBObject();
        	notInCondition.put("$nin", excludeList);
        	query.put(DrawDBClient.F_VIEW_USER_LIST, notInCondition);
    		
        	GameLog.info(sessionId, "<randomGetDraw> query = "+query.toString());
    		obj = dbClient.findOne(DrawDBClient.T_DRAW, query);
    		
    	}
    	
		if (obj != null){
			BasicDBObject keyQuery = new BasicDBObject();
			keyQuery.put("_id", obj.get("_id"));
			GameLog.info(sessionId, "<randomGetDraw> update = "+keyQuery.toString() + ", update="+update.toString());
			dbClient.findAndModify(DrawDBClient.T_DRAW, keyQuery, update);
		}

		if (obj == null){
    		GameLog.info(sessionId, "random fetch data from DB, but no record return");
    		return null;
    	}
    	
    	byte[] data = (byte[])obj.get(DrawDBClient.F_DRAW_DATA);
    	if (data == null){
    		GameLog.info(sessionId, "random fetch data from DB, but draw data null?");
    		return null;    		
    	}
    	
    	GameLog.info(sessionId, "random fetch data from DB, data bytes="+data.length);
    	return (BasicDBObject)obj;
    }
    
	public void storeGuessWord(final int sessionId, final String word, final int language,
			final Collection<String> collection) {
		
		executor.execute(new Runnable(){

			@Override
			public void run() {
				
				if (word == null || collection == null || collection.size() == 0)
					return;
				
				String wordText = word.toUpperCase();
				
				// insert data into mongo DB here
				MongoDBClient dbClient = DrawDBClient.getInstance().getMongoClient();
				
				BasicDBObject query = new BasicDBObject();
				query.put(DrawDBClient.F_WORD, wordText);
				
				BasicDBList eachValueList = new BasicDBList();
				eachValueList.addAll(collection);
				
				BasicDBObject eachValue = new BasicDBObject();
		    	eachValue.put("$each", eachValueList);
		    	
		    	BasicDBObject addToSetValue = new BasicDBObject (); 
		    	addToSetValue.put(DrawDBClient.F_GUESS_WORD_LIST, eachValue);
		    	
		    	BasicDBObject setValue = new BasicDBObject();
		    	setValue.put(DrawDBClient.F_WORD, wordText);
		    	
		    	BasicDBObject update = new BasicDBObject ();
		    	update.put("$addToSet", addToSetValue);
		    	update.put("$set", setValue);
		    					
		    	GameLog.info(sessionId, "<storeGuessWord> query="+query.toString()+", update="+update.toString());
				GameLog.info(sessionId, "save guess word data into DB, word count ="+collection.size());
				dbClient.updateOrInsert(DrawDBClient.T_GUESS, query, update);				
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
		query.put(DrawDBClient.F_WORD, word.toUpperCase());
				
		DBObject obj = dbClient.findOne(DrawDBClient.T_GUESS, query);
		if (obj == null)
			return null;
		
		BasicDBList list = (BasicDBList)obj.get(DrawDBClient.F_GUESS_WORD_LIST);
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
