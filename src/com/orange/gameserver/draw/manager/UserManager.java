package com.orange.gameserver.draw.manager;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import com.orange.gameserver.draw.dao.User;

public class UserManager {
	
	ConcurrentHashMap<String, User> onlineUserMap = new ConcurrentHashMap<String, User>();
	Object transactionLock = new Object();
	protected static final Logger logger = Logger.getLogger(UserManager.class
			.getName());
	
	private static UserManager userManager = null;
	private UserManager() {
	}
	public static UserManager getInstance() {
		if (userManager == null) {
			userManager = new UserManager();
		}
		return userManager;
	}
	
	public void removeOnlineUserById(String userId) {
		logger.info("<removeOnlineUserById> userId= " + userId);
		if (userId != null) {			
			this.onlineUserMap.remove(userId);	
		}	
	}
	
	public User findUserById(String userId) {
		return onlineUserMap.get(userId);
	}
	
	public void addOnlineUser(String userId, String nickName, String avatar, Channel channel,
			int sessionId) {
		
		if (userId == null || channel == null)
			return;
		
		User user = new User(userId, nickName, avatar, channel, sessionId);		
		User userFound = onlineUserMap.putIfAbsent(userId, user);
		if (userFound != null)
			user = userFound;
		
		user.setCurrentSessionId(sessionId);
		logger.info("<addOnlineUser> userId= " + userId + ", nick=" + nickName + " at session " + sessionId);
	}
	
	public int findGameSessionIdByUserId(String userId){
		User user = findUserById(userId);
		if (user == null)
			return -1;
		
		return user.getCurrentSessionId();
	}
	
}
