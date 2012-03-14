package com.orange.gameserver.hit.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.UserAtGame;


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

	public UserAtGame userLogin(String userId, String nickName, Channel channel){
		UserAtGame user = new UserAtGame(userId, nickName, channel);
		userMap.put(userId, user);
		return user;
	}
	
}
