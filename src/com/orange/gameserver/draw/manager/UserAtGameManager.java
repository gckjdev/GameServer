package com.orange.gameserver.draw.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.UserAtGame;


public class UserAtGameManager {
	
	ConcurrentMap<String, UserAtGame> userMap = new ConcurrentHashMap<String, UserAtGame>();
	
	// thread-safe singleton implementation
    private static UserAtGameManager manager = new UserAtGameManager();     
    private UserAtGameManager(){		
	} 	    
    public static UserAtGameManager getInstance() { 
    	return manager; 
    }
    
	public UserAtGame findUserById(String userId) {
		if (userMap.containsKey(userId))
			return userMap.get(userId);
			
		return null;
	} 
	
}
