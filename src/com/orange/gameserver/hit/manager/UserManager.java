package com.orange.gameserver.hit.manager;

import java.util.concurrent.ConcurrentHashMap;

import com.orange.gameserver.hit.dao.User;
public class UserManager {
	
	ConcurrentHashMap<String, User> onlineUserMap;
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
	
	public void addOnlineUser(User user) {
		if (user != null && user.getUserId() != null) {
			onlineUserMap.put(user.getUserId(), user);	
		}
	}
	
	public void removeOnlineUserById(String userId) {
		if (userId != null) {
			this.onlineUserMap.remove(userId);	
		}	
	}
	
	public User findUserById(String userId) {
		return onlineUserMap.get(userId);
	}
	
}
