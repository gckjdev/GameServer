package com.orange.gameserver.draw.manager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.orange.gameserver.draw.dao.User;

public class GameSessionUserManager {

	protected static final Logger logger = Logger.getLogger("GameSessionUserManager");
	
	ConcurrentMap<Integer, ConcurrentSkipListSet<User>> sessionUserMap = 
		new ConcurrentHashMap<Integer, ConcurrentSkipListSet<User>>();
	
	// thread-safe singleton implementation
    private static GameSessionUserManager manager = new GameSessionUserManager(); 
    
    private GameSessionUserManager(){		
	} 	    
    
    public static GameSessionUserManager getInstance() { 
    	return manager; 
    } 

    public void addUserIntoSession(User user, int sessionId){    	

    	logger.info("<addUserIntoSession> user="+user.getNickName()+", sessionId="+sessionId);    	    	
    	ConcurrentSkipListSet<User> users = new ConcurrentSkipListSet<User>();
    	ConcurrentSkipListSet<User> usersFound = sessionUserMap.putIfAbsent(sessionId, users);
    	if (usersFound != null){
    		users = usersFound;
    	}

    	// add user and set user data
    	users.add(user);
    	user.setCurrentSessionId(sessionId);
		return;
    }
    
    public void removeUserFromSession(User user, int sessionId){
    	logger.info("<removeUserFromSession> user="+user.getNickName()+", sessionId="+sessionId);
    	sessionUserMap.remove(sessionId, user);
    }
    
    
    
}
