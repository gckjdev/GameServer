package com.orange.gameserver.draw.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.DrawGameSession;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;


public class GameSessionManager {
		
	protected static final Logger logger = Logger.getLogger("GameManager");

	public static final int NO_SESSION_MATCH_FOR_USER = -1; 
	public static final int GAME_SESSION_COUNT = 1000;
	public static final int MAX_USER_PER_GAME_SESSION = 6;

	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	
	//use three sets to classify the game sessions
	ConcurrentHashSet<Integer> candidateSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> freeSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> fullSet = new ConcurrentHashSet<Integer>();
	
	// lock candidate/free/full set
	Object sessionUserLock = new Object();
	
	// a map to store game session
	ConcurrentMap<Integer, GameSession> gameCollection = new ConcurrentHashMap<Integer, GameSession>();
	
	// thread-safe singleton implementation
    private static GameSessionManager manager = new GameSessionManager();     
    private GameSessionManager(){		
    	initAllGameSession(GAME_SESSION_COUNT);
    	logger.info("<GameManager> init");
//    	logger.info("hashMap:"+gameCollection);
	} 	    
    public static GameSessionManager getInstance() { 
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
	
	public int allocGameSessionForUser(String userId, String nickName, String avatar, boolean gender, 
			Channel channel, Set<Integer> excludeSessionSet) {		
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
				int userCount = addUserIntoSession(userId, nickName, avatar, gender, channel, session);
				
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
			UserManager.getInstance().addOnlineUser(userId, nickName, avatar, gender, channel, sessionId);
		}
		
		ChannelUserManager.getInstance().addUserIntoChannel(channel, userId);		
		return sessionId;
	}
	
	public void adjustSessionSetForPlaying(GameSession session) {
		synchronized(sessionUserLock){
			int sessionId = session.getSessionId();
			candidateSet.remove(sessionId);
			freeSet.remove(sessionId);						
		}
	}
	
	public void adjustSessionSetForWaiting(GameSession session){
		synchronized(sessionUserLock){
			int sessionId = session.getSessionId();
			int userCount = sessionUserManager.getSessionUserCount(sessionId);
			
			// adjust candidate and full set, also add user
			if (userCount >= MAX_USER_PER_GAME_SESSION){
				fullSet.add(sessionId);
			}
			else if (userCount >= MAX_USER_PER_GAME_SESSION - 2){
				candidateSet.add(sessionId);
			}				
			else{
				freeSet.add(sessionId);
			}
		}		
	}
	
	public void adjustSessionSet(GameSession session) {
		synchronized(sessionUserLock){
			
			int sessionId = session.getSessionId();
			int userCount = sessionUserManager.getSessionUserCount(sessionId);
			
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
//			session.removeUser(userId); 			
			sessionUserManager.removeUserFromSession(userId, session.getSessionId());
			adjustSessionSet(session);
			UserManager.getInstance().removeOnlineUserById(userId);
	}
	
	private int addUserIntoSession(String userId, String nickName, String avatar, boolean gender, Channel channel, GameSession session){
			User user = new User(userId, nickName, avatar, gender, channel, session.getSessionId());
			sessionUserManager.addUserIntoSession(user, session);
			return 0;
	}		
	
	public void printSets() {		
		logger.info("<Free Set> : " + freeSet);
		logger.info("<Candidate Set> : " + candidateSet);
		logger.info("<Full Set> : " + fullSet);		
	}
	
	public GameCompleteReason isSessionTurnFinish(GameSession session) {
		int userCount = sessionUserManager.getSessionUserCount(session.getSessionId());
		if (userCount == 1)
			return GameCompleteReason.REASON_ONLY_ONE_USER; 
		
		if (session.isAllUserGuessWord(userCount)){
			return GameCompleteReason.REASON_ALL_USER_GUESS;
		}
		
		return GameCompleteReason.REASON_NOT_COMPLETE;
	}
	
	public void adjustCurrentPlayerForUserQuit(GameSession session, String quitUserId) {
		List<User> userList = GameSessionUserManager.getInstance().getUserListBySession(session.getSessionId());
		int index = 0;
		boolean userFound = false;
		for (User user : userList){
			if (user.getUserId().equals(quitUserId)){
				userFound = true;
				if (index >= 1){
					// has previous user
					session.setCurrentPlayUser(userList.get(index - 1));
				}
				else{
					// user is the first user
					session.setCurrentPlayUser(null);
				}
				break;
			}
			index ++;
		}
		
		if (userFound){
			
		}
	}
	

}
