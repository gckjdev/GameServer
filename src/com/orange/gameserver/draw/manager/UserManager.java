package com.orange.gameserver.draw.manager;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.server.GameService;
public class UserManager {
	
	ConcurrentHashMap<String, User> onlineUserMap;
	Object transactionLock = new Object();
	protected static final Logger logger = Logger.getLogger(UserManager.class
			.getName());
	
	private static UserManager userManager = null;
	private UserManager() {
		super();
		onlineUserMap = new ConcurrentHashMap<String, User>();
	}
	public static UserManager getInstance() {
		if (userManager == null) {
			userManager = new UserManager();
		}
		return userManager;
	}
	
	/*
	private void addOnlineUser(User user) {
		if (user != null && user.getUserId() != null) {
			onlineUserMap.put(user.getUserId(), user);	
		}
	}
	*/
	
	public void removeOnlineUserById(String userId) {
		synchronized(transactionLock){
			if (userId != null) {
				this.onlineUserMap.remove(userId);	
			}	
		}
	}
	
	public User findUserById(String userId) {
		return onlineUserMap.get(userId);
	}
	
	public void addOnlineUser(String userId, String nickName, String avatar, Channel channel,
			int sessionId) {
		
		if (userId == null || channel == null)
			return;
		
		synchronized(transactionLock){
			User user = findUserById(userId);
			if (user == null){
				user = new User(userId, nickName, avatar, channel, sessionId);
				onlineUserMap.put(userId, user);		
				logger.info("<addOnlineUser> Create " + userId + ", nick=" + nickName + " at session " + sessionId);
			}
			else{
				user.setChannel(channel);
				user.setNickName(nickName);
				user.setCurrentSessionId(sessionId);
				logger.info("<addOnlineUser> Update " + userId + ", nick=" + nickName + " at session " + sessionId);
			}			
		}
	}
	
	public int findGameSessionIdByUserId(String userId){
		synchronized(transactionLock){
			User user = findUserById(userId);
			if (user == null)
				return -1;
			
			return user.getCurrentSessionId();
		}
	}
	
}
