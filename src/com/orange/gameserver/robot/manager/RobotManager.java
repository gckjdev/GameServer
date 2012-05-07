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
import org.eclipse.jetty.util.ConcurrentHashSet;

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

    public static final int MAX_ROBOT_USER = 10;
    
    final String USER_NAME_LIST[] = {"Jenny", "Mike", "Tina", "Robert", "Nancy", "Vivian", "Andy", "Cindy", "Judy", "Julie"};
    final boolean USER_GENDER_LIST[] = {false, true, false, true, false, false, true, false, false, false};
    final String USER_AVATAR_LIST[] = {
    		"http://www.ttoou.com/qqtouxiang/allimg/111111/3-111111220531.jpg", 
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111113/3-111113230948.jpg", 
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111111/3-111111220533.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111113/3-111113230957.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111111/3-111111220541.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G647.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111224/1-111224200641.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160GA3.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G648.jpg",
    		
    		"http://www.ttoou.com/qqtouxiang/allimg/111216/1-1112160G645-50.jpg",    		    		
    		};
    public final static String ROBOT_USER_ID_PREFIX = "robot_$$_";     
    
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
    	
    	String userId = ROBOT_USER_ID_PREFIX + index;
    	String nickName = USER_NAME_LIST[index];
    	String avatar = USER_AVATAR_LIST[index];
    	boolean gender = USER_GENDER_LIST[index];
    	
    	RobotClient client = new RobotClient(userId, nickName, avatar, gender, sessionId, index);    	
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
}
