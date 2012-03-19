package com.orange.gameserver.hit.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;


public class GameManager {
		
	protected static final Logger logger = Logger.getLogger("GameManager");

	public static final int NO_SESSION_MATCH_FOR_USER = -1; 
	public static final int GAME_SESSION_COUNT = 1000;
	public static final int MAX_USER_PER_GAME_SESSION = 6;

	//use three sets to classify the game sessions
	ConcurrentHashSet<Integer> candidateSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> freeSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> fullSet = new ConcurrentHashSet<Integer>();
	
	// lock candidate/free/full set
	Object sessionUserLock = new Object();
	
	// a map to store game session
	ConcurrentMap<Integer, GameSession> gameCollection = new ConcurrentHashMap<Integer, GameSession>();
	
	// thread-safe singleton implementation
    private static GameManager manager = new GameManager();     
    private GameManager(){		
    	initAllGameSession(GAME_SESSION_COUNT);
    	logger.info("<GameManager> init");
//    	logger.info("hashMap:"+gameCollection);
	} 	    
    public static GameManager getInstance() { 
    	return manager; 
    } 
	
//    public int getGameSessionSize () {
//		return sessionIdIndex.get();
//	}
    
//	public DrawGameSession createNewDrawGameSession(String userId) {
//		int sessionId = sessionIdIndex.incrementAndGet();	
//		String roomName = roomNumber.incrementAndGet() + "";
//		DrawGameSession session = new DrawGameSession(sessionId, roomName, userId);
//		gameCollection.put(Integer.valueOf(sessionId), session);		
//		freeSet.add(sessionId);
//		return session;
//	}
		
	public void initAllGameSession(int count)
	{
		synchronized(sessionUserLock){
			for (int i = 1; i <= count; i++) {
				int sessionId = i;	
				String roomName = i + "";
				DrawGameSession session = new DrawGameSession(sessionId, roomName, null);
				gameCollection.put(Integer.valueOf(sessionId), session);
				freeSet.add(sessionId);
			}
			
			this.printSets();
		}
	}
		
	public GameSession findGameSessionById(int id) {
		return gameCollection.get(id);		
	}

	private int getSessionFromSet(Set<Integer> set, Set<Integer> excludeSessionSet){
		int sessionId = -1;
		if (set.isEmpty())
			return -1;
		
		if (excludeSessionSet == null){
			sessionId = set.iterator().next().intValue();
			return sessionId;
		}		
		
		Set<Integer> diffSet = new HashSet<Integer>();
		diffSet.addAll(set);
		diffSet.removeAll(excludeSessionSet);
		if (diffSet.isEmpty())
			return -1;
		
		sessionId = diffSet.iterator().next().intValue();
		return sessionId;
	}
	
	public int allocGameSessionForUser(String userId, String nickName, Channel channel, Set<Integer> excludeSessionSet) {		
		int sessionId = NO_SESSION_MATCH_FOR_USER;
		synchronized(sessionUserLock){
			
			ConcurrentHashSet<Integer> currentSet = null;
			
			sessionId = getSessionFromSet(candidateSet, excludeSessionSet);
			if (sessionId != -1){
				currentSet = candidateSet;
			}

			if (sessionId == -1){
				sessionId = getSessionFromSet(freeSet, excludeSessionSet);
				if (sessionId != -1){
					currentSet = freeSet;
				}
			}
						
			if (sessionId != -1){
				
				// add user into game session
				GameSession session = this.findGameSessionById(sessionId);
				int userCount = addUserIntoSession(userId, nickName, channel, session);
				
				// adjust candidate and full set, also add user
				if (userCount >= MAX_USER_PER_GAME_SESSION){
					currentSet.remove(sessionId);
					fullSet.add(sessionId);
				}
				else if (userCount >= MAX_USER_PER_GAME_SESSION - 2){
					currentSet.remove(sessionId);
					candidateSet.add(sessionId);
				}
			}
			
			
		}		
		
		if (sessionId != -1){
			UserManager.getInstance().addOnlineUser(userId, nickName, channel, sessionId);
		}
		
		ChannelUserManager.getInstance().addUserIntoChannel(channel, userId);		
		return sessionId;
	}
	
	public void adjustSessionSet(GameSession session) {
		synchronized(sessionUserLock){
			
			int sessionId = session.getSessionId();
			int userCount = session.getUserCount();
			
			if (userCount >= MAX_USER_PER_GAME_SESSION){
				candidateSet.remove(sessionId);
				freeSet.remove(sessionId);
				fullSet.add(sessionId);
			}
			else if (userCount >= MAX_USER_PER_GAME_SESSION - 2){
				freeSet.remove(sessionId);
				fullSet.remove(sessionId);
				candidateSet.add(sessionId);
			}
			else{
				candidateSet.remove(sessionId);
				fullSet.remove(sessionId);
				freeSet.add(sessionId);				
			}
		}
	}
	
	public void removeUserFromSession(String userId, GameSession session){
			session.removeUser(userId); 			
			adjustSessionSet(session);
			UserManager.getInstance().removeOnlineUserById(userId);
	}
	
	private int addUserIntoSession(String userId, String nickName, Channel channel, GameSession session){
			return session.addUser(userId, nickName, channel);
	}		
	
	public void printSets() {		
		logger.info("<Free Set> : " + freeSet);
		logger.info("<Candidate Set> : " + candidateSet);
		logger.info("<Full Set> : " + fullSet);		
	}
	

}
