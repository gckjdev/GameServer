package com.orange.gameserver.draw.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class GameSessionUserManager {

	protected static final Logger logger = Logger.getLogger("GameSessionUserManager");
	public static int MAX_USER_PER_SESSION = 6;
	
	ConcurrentMap<Integer, CopyOnWriteArrayList<User>> sessionUserMap = 
		new ConcurrentHashMap<Integer, CopyOnWriteArrayList<User>>();
	
	// thread-safe singleton implementation
    private static GameSessionUserManager manager = new GameSessionUserManager(); 
    
    public void initMaxUserPerSession(){    		
    	String value = System.getProperty("game.maxsessionuser");
		if (value != null && !value.isEmpty()){
			MAX_USER_PER_SESSION = Integer.parseInt(value);
		}
		else{
			MAX_USER_PER_SESSION = 6; // default
		}
		
		logger.info("set MAX_USER_PER_SESSION to "+MAX_USER_PER_SESSION);
    }
    
    private GameSessionUserManager(){		
    	initMaxUserPerSession();
	} 	    
    
    public static GameSessionUserManager getInstance() { 
    	return manager; 
    } 

    public void setUserPlaying(GameSession session){
    	int sessionId = session.getSessionId();
    	CopyOnWriteArrayList<User> userList = sessionUserMap.get(sessionId);
    	for (User user : userList){
    		user.setPlaying(true);
    		GameLog.info(sessionId, "set user "+user.getNickName()+ " PLAYING");
    	}
    }

    public void clearUserPlaying(GameSession session){
    	int sessionId = session.getSessionId();
    	CopyOnWriteArrayList<User> userList = sessionUserMap.get(sessionId);
    	for (User user : userList){
    		user.setPlaying(false);
    		GameLog.info(sessionId, "set user "+user.getNickName()+ " NOT PLAYING");
    	}
    }
    
    public int addUserIntoSession(User user, GameSession session){    	

    	int sessionId = session.getSessionId();
    	CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    	CopyOnWriteArrayList<User> usersFound = sessionUserMap.putIfAbsent(sessionId, users);
    	if (usersFound != null){
    		users = usersFound;
    	}

    	// add user and set user data
		if (users.addIfAbsent(user)){
			user.setCurrentSessionId(sessionId);
    		GameLog.info(sessionId, "<addUserIntoSession> user="+user.getNickName()+", sessionId="+sessionId);

    		int size = users.size();
        	if (size == 1 || (size > 0 && session.getCurrentPlayUser() == null)){
        		User firstUser = users.get(0);
        		session.setCurrentPlayUser(firstUser, 0);
        		GameLog.info(sessionId, "<addUserIntoSession> set first user " + firstUser + " as current user");
        	}
        	
        	return size;
		}
		else{
			return users.size();
		}    	    	
    }
    
    public void removeUserFromSession(String userId, int sessionId){
    	CopyOnWriteArrayList<User> users = sessionUserMap.get(sessionId);
    	if (users == null){
    		GameLog.info(sessionId, "<removeUserFromSession> session not found, user="+userId+", sessionId="+sessionId);    	
    		return;
    	}
    	
    	User userFound = null;
    	for (User user : users){
    		if (user.getUserId().equalsIgnoreCase(userId)){
    			userFound = user;
    			break;
    		}
    	}
    	
    	if (userFound != null){
    		GameLog.info(sessionId, "<removeUserFromSession> user="+userFound+", sessionId="+sessionId);
    		users.remove(userFound);
    	}
    	else{
    		GameLog.info(sessionId, "<removeUserFromSession> cannot find user, user="+userId+", sessionId="+sessionId);    		
    	}
    }
    
    public List<User> getUserListBySession(int sessionId){
    	CopyOnWriteArrayList<User> set = sessionUserMap.get(sessionId);
    	if (set == null){
    		return Collections.emptyList();
    	}
    	else{
    		return set;
    	}
    }
    
    public int getSessionUserCount(int sessionId){
    	CopyOnWriteArrayList<User> set = sessionUserMap.get(sessionId);
    	if (set == null){
    		return 0;
    	}
    	else{
    		return set.size();
    	}
    }
    
    
	public List<PBGameUser> usersToPBUsers(int sessionId) {
		CopyOnWriteArrayList<User> userSet = sessionUserMap.get(sessionId);
		if (userSet == null)
			return Collections.emptyList();
		
		List<PBGameUser> list = new ArrayList<PBGameUser>();
		for (User user : userSet){
			GameBasicProtos.PBGameUser gameUser = GameBasicProtos.PBGameUser.newBuilder()
																				.setUserId(user.getUserId())																				
																				.setNickName(user.getNickName())
																				.setAvatar(user.getAvatar())
																				.setGender(user.getGender())
																				.setLocation(user.getLocation())
																				.addAllSnsUsers(user.getSnsUser())
																				.build();
			list.add(gameUser);
		}
		return list;
	}
	
	public boolean isSessionFull(int sessionId) {
		CopyOnWriteArrayList<User> userSet = sessionUserMap.get(sessionId);
		if (userSet == null){
			return false;
		}
		
		return userSet.size() >= MAX_USER_PER_SESSION ? true : false;
	}	
    
	public boolean isSessionEmpty(int sessionId) {
		CopyOnWriteArrayList<User> userSet = sessionUserMap.get(sessionId);
		if (userSet == null){
			return true;
		}
		
		return (userSet.size() == 0);
	}

	/*
	@Deprecated
	private void chooseNewPlayUser(GameSession session) {
		int sessionId = session.getSessionId();
		CopyOnWriteArrayList<User> users = sessionUserMap.get(sessionId);
		if (users == null){
    		GameLog.info(sessionId, "<chooseNewPlayUser> but sessionId not found in session user map");								
			return;
		}
		
		Iterator<User> iter = users.iterator();
		if (iter == null){
    		GameLog.info(sessionId, "<chooseNewPlayUser> but no user");								
			return;
		}
		
		User userSelected = null;
		while (iter.hasNext()){
			User user = iter.next();
			if (session.isCurrentPlayUser(user.getUserId())){
				if (iter.hasNext()){
					// use next one as current play user				
					User nextUser = iter.next(); 
					session.setCurrentPlayUser(nextUser, 0);
					userSelected = nextUser;
		    		GameLog.info(sessionId, "<chooseNewPlayUser> choose next user for play, user = " + nextUser);					
				}
				else{
					// use the first one as current play user
					Iterator<User> newIter = users.iterator();
					if (newIter != null && newIter.hasNext()){
						User firstUser = newIter.next();
						session.setCurrentPlayUser(firstUser, 0);
						userSelected = firstUser;
						GameLog.info(sessionId, "<chooseNewPlayUser> set first user for play, user = " + firstUser);					
					}
				}
			}
		}
		
		if (userSelected == null){
			if (users.size() > 0){
				User firstUser = users.get(0);
				GameLog.info(sessionId, "<chooseNewPlayUser> set first user for play, user = " + firstUser);					
				session.setCurrentPlayUser(firstUser, 0);
			}
			else{
				GameLog.info(sessionId, "<chooseNewPlayUser> no user, clear user for play");					
				session.setCurrentPlayUser(null, 0);
			}
		}
	}
		*/
}
