package com.orange.gameserver.draw.manager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.robot.manager.RobotManager;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;

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
		logger.info("<removeOnlineUserById> userId= " + userId + ", user count = " + onlineUserMap.size());
		if (userId != null) {			
			this.onlineUserMap.remove(userId);
		}	
	}
	
	public User findUserById(String userId) {
		return onlineUserMap.get(userId);
	}
	
	public void addOnlineUser(User user) {

		if (user == null)
			return;
		
		String userId = user.getUserId();
		
		// remove old user channel if needed
		User oldUser = onlineUserMap.get(userId);
		if (oldUser != null){
			ChannelUserManager.getInstance().removeChannel(user.getChannel());
		}
		
		// add new user
		User userFound = onlineUserMap.put(userId, user);
		if (userFound != null)
			user = userFound;
		
		logger.info("<addOnlineUser> userId= " + userId + ", nick=" + user.getNickName() + " at session " + user.getCurrentSessionId()  + ", user count = " + onlineUserMap.size());
	}
	
	public int findGameSessionIdByUserId(String userId){
		User user = findUserById(userId);
		if (user == null)
			return -1;
		
		return user.getCurrentSessionId();
	}
	
	public int getOnlineUserCount(){
		return onlineUserMap.size() * 2 + RobotManager.MAX_ROBOT_USER;
	}
		
}
