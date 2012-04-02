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
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;

public class GameSessionUserManager {

	protected static final Logger logger = Logger.getLogger("GameSessionUserManager");
	private static final int MAX_USER_PER_SESSION = 6;
	
	ConcurrentMap<Integer, CopyOnWriteArrayList<User>> sessionUserMap = 
		new ConcurrentHashMap<Integer, CopyOnWriteArrayList<User>>();
	
	// thread-safe singleton implementation
    private static GameSessionUserManager manager = new GameSessionUserManager(); 
    
    private GameSessionUserManager(){		
	} 	    
    
    public static GameSessionUserManager getInstance() { 
    	return manager; 
    } 

    public void addUserIntoSession(User user, GameSession session){    	

    	int sessionId = session.getSessionId();
    	CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    	CopyOnWriteArrayList<User> usersFound = sessionUserMap.putIfAbsent(sessionId, users);
    	if (usersFound != null){
    		users = usersFound;
    	}

    	// add user and set user data
		if (users.addIfAbsent(user)){
			user.setCurrentSessionId(sessionId);
    		logger.info("<addUserIntoSession> user="+user.getNickName()+", sessionId="+sessionId);

        	if (users.size() == 1){
        		User firstUser = users.get(0);
        		session.setCurrentPlayUser(firstUser);
        		logger.info("<addUserIntoSession> init first user as current, user = " + firstUser);
        	}
		}
    	    	
    	return;
    }
    
    public void removeUserFromSession(String userId, int sessionId){
    	CopyOnWriteArrayList<User> users = sessionUserMap.get(sessionId);
    	if (users == null){
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
        	logger.info("<removeUserFromSession> user="+userFound.getNickName()+", sessionId="+sessionId);
    		users.remove(userFound);
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
		
		return userSet.size() == 0 ? true : false;
	}

	public void chooseNewPlayUser(GameSession session) {
		CopyOnWriteArrayList<User> users = sessionUserMap.get(session.getSessionId());
		Iterator<User> iter = users.iterator();
		if (iter == null)
			return;
		
		while (iter.hasNext()){
			User user = iter.next();
			if (session.isCurrentPlayUser(user.getUserId())){
				if (iter.hasNext()){
					// use next one as current play user				
					User nextUser = iter.next(); 
					session.setCurrentPlayUser(nextUser);
		    		logger.info("<addUserIntoSession> init first user as current, user = " + nextUser);					
				}
				else{
					// use the first one as current play user
					Iterator<User> newIter = users.iterator();
					if (newIter != null && newIter.hasNext()){
						User firstUser = newIter.next();
						session.setCurrentPlayUser(firstUser);
			    		logger.info("<addUserIntoSession> init first user as current, user = " + firstUser);					
					}
				}
			}
		}
	}
}
