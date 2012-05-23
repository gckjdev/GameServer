package com.orange.gameserver.robot.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.cassandra.cli.CliParser.newColumnFamily_return;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.game.constants.DBConstants;
import com.orange.game.model.dao.User;
import com.orange.gameserver.db.DrawDBClient;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.gameserver.robot.client.RobotClient;

public class RobotManager {

	// thread-safe singleton implementation
    private static RobotManager manager = new RobotManager();     
    private RobotManager(){
    	robotCount = getRobotCount();
    	for (int i=0; i<robotCount; i++)
    		freeSet.add(i);
	} 	    
    public static RobotManager getInstance() { 
    	return manager; 
    }
    
    public static final Logger log = Logger.getLogger(RobotManager.class.getName()); 

    public static final int MAX_ROBOT_USER = 8;
    public static long robotCount = 0;

    public final static String ROBOT_USER_ID_PREFIX = "999999999999999999999";     
    
    ConcurrentHashSet<Integer> allocSet = new ConcurrentHashSet<Integer>();
    ConcurrentHashSet<Integer> freeSet  = new ConcurrentHashSet<Integer>();
    Object allocLock = new Object();
    
    
    
    public static boolean isRobotUser(String userId){
    	return userId.contains(ROBOT_USER_ID_PREFIX);
    }
    
    public int allocIndex(){
    		if (freeSet.isEmpty() ||
    			freeSet.iterator() == null)
    			return -1;
    		
    		Random random = new Random();
    		random.setSeed(System.currentTimeMillis());
    		int randomCount = random.nextInt(freeSet.size());
    		Iterator<Integer> iter = freeSet.iterator();
    		
    		
    		int index = 0;
    		while (iter != null && iter.hasNext() && index < randomCount){
    			index++;
    			iter.next();
    		}
    		
    		if (iter != null && iter.hasNext()){
    			index = iter.next().intValue();
    		}
    		
    		if (index == -1)
    			return -1;
    		
    		allocSet.add(index);
    		freeSet.remove(index);

    		GameLog.info(0, "alloc robot, alloc index="+index + ", active robot count = "+allocSet.size());
    		return index;
    }
    
    public void deallocIndex(int index){
    	if (!isValidIndex(index) && index < robotCount){
    		return;
    	}
    	
//    	synchronized(allocLock){
    		freeSet.add(index);
    		allocSet.remove(index);
    	
    	GameLog.info(0, "dealloc robot, index="+index + ", active robot count = "+allocSet.size());
    }
    
    public RobotClient allocNewClient(int sessionId) {        	
    	
    	int index = allocIndex();    	
    	if (!isValidIndex(index)){
    		return null;
    	}
    	
    	String nickName = "";
    	String userId = ROBOT_USER_ID_PREFIX+"000";
    	String avatar = "";
    	boolean gender = false;
    	String location = "";
    	
    	User robotUser = findRobotByIndex(index);
    	if (robotUser != null) {
    		nickName = robotUser.getNickName();
        	userId = robotUser.getUserId();
        	avatar = robotUser.getAvatar();
        	gender = (robotUser.getGender() == "m");
        	location = robotUser.getLocation();
		}
    	   	
    	RobotClient client = new RobotClient(userId, nickName, avatar, gender, location, sessionId, index);    	
		return client;
	}
	
    private boolean isValidIndex(int index) {
		return (index >= 0);
	}
	
	public void deallocClient(RobotClient robotClient) {
		if (robotClient == null)
			return;
		
		this.deallocIndex(robotClient.getClientIndex());				
	} 
	
	public long getRobotCount() {
		MongoDBClient mongoClient = DrawDBClient.getInstance().getMongoClient();
		long count = mongoClient.count(DBConstants.T_USER, new BasicDBObject(DBConstants.F_ISROBOT, 1));
		return count;
	}
	
	public User findRobotByIndex (int index) {
		MongoDBClient mongoClient = DrawDBClient.getInstance().getMongoClient();
		String userId = ROBOT_USER_ID_PREFIX+String.format("%03d", index);
		if (mongoClient == null || userId == null || userId.length() <= 0 || index > 999)
            return null;

        DBObject obj = mongoClient.findOne(DBConstants.T_USER, DBConstants.F_USERID, new ObjectId(userId));
        if (obj == null)
            return null;

        return new User(obj);
	}
}
