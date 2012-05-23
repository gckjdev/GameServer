package com.orange.gameserver.robot.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.eclipse.jetty.util.ConcurrentHashSet;

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
    	for (int i=0; i<USER_NAME_LIST.length; i++)
    		freeSet.add(i);
	} 	    
    public static RobotManager getInstance() { 
    	return manager; 
    }
    
    public static final Logger log = Logger.getLogger(RobotManager.class.getName()); 

    public static final int MAX_ROBOT_USER = 8;
    
    final String USER_NAME_LIST[] = {"Lily@Moon", "LikeAFox", "Tina", "Hugo", "Jan Vans", "Vivian", "Johnson", "Allen J"}; //, "Miaotiao", "Julie"};
    final String USER_LOCATION_LIST[] = {"UK", "LA USA", "New York, USA", "UK", "LA USA", "UK", "New York, USA", "LA USAJ"}; //, "Miaotiao", "Julie"};
    final boolean USER_GENDER_LIST[] = {false, true, false, true, true, false, true, false, false, false};
    final String USER_AVATAR_LIST[] = {
//    		"http://www.ttoou.com/qqtouxiang/allimg/111111/3-111111220531.jpg", 
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/120504/co120504104A9-1-lp.jpg", 
    		
//    		"http://www.ttoou.com/qqtouxiang/allimg/111111/3-111111220533.jpg",

    		"http://www.ttoou.com/qqtouxiang/allimg/120407/co12040FZ942-5-lp.jpg",
    		
//    		"http://www.ttoou.com/qqtouxiang/allimg/111113/3-111113230957.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111107/3-11110H30558.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/120416/co120416093105-0-lp.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/120421/co120421091P1-6-lp.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G647.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/120421/co120421091P1-5-lp.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/120404/co120404100521-6-lp.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G648.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G645-50.jpg",    		    		
    		};
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
    	if (!isValidIndex(index)){
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
		return (index >= 0 && index < USER_NAME_LIST.length);
	}
	
	public void deallocClient(RobotClient robotClient) {
		if (robotClient == null)
			return;
		
		this.deallocIndex(robotClient.getClientIndex());				
	} 
	
	public void initRobot() {
		
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
