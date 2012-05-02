package com.orange.gameserver.robot.manager;

import java.util.Iterator;
import java.util.Random;

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

    final String USER_NAME_LIST[] = {"Jenny", "Mike", "Tina"};
    final boolean USER_GENDER_LIST[] = {false, true, false};
    final String USER_AVATAR_LIST[] = {"http://icons-search.com/img/yellowicon/game_star_lin.zip/Linux-Pacman_256x256.png-256x256.png", "http://smasherentertainment.files.wordpress.com/2008/06/ninja.png", "http://tux.crystalxp.net/png/caporal-tux-capo-5832.png"};
    public final static String ROBOT_USER_ID_PREFIX = "robot";     
    
    ConcurrentHashSet<Integer> allocSet = new ConcurrentHashSet<Integer>();
    ConcurrentHashSet<Integer> freeSet  = new ConcurrentHashSet<Integer>();
    Object allocLock = new Object();
    
    public int allocIndex(){
    	synchronized(allocLock){
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

    		GameLog.info(0, "alloc index, random count = "+randomCount+ ", alloc index="+index);
    		
    		if (index == -1)
    			return -1;
    		
    		allocSet.add(index);
    		freeSet.remove(index);
    		return index;
    	}    	
    }
    
    public void deallocIndex(int index){
    	if (!isValidIndex(index)){
    		return;
    	}
    	
    	synchronized(allocLock){
    		freeSet.add(index);
    		allocSet.remove(index);
    	}
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
