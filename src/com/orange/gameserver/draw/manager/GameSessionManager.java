package com.orange.gameserver.draw.manager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.DrawGameSession;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;


public class GameSessionManager {
		
	protected static final Logger logger = Logger.getLogger("GameManager");

	public static final int NO_SESSION_MATCH_FOR_USER = -1; 
	public static final int GAME_SESSION_COUNT = 1000;

	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	
	//use three sets to classify the game sessions
	ConcurrentHashSet<Integer> candidateSet = new ConcurrentHashSet<Integer>();
//	ConcurrentHashSet<Integer> candidateSet2 = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> freeSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> fullSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> playSet = new ConcurrentHashSet<Integer>();
	
	
	// lock candidate/free/full set
	Object sessionUserLock = new Object();
	
	// a map to store game session
	ConcurrentMap<Integer, GameSession> gameCollection = new ConcurrentHashMap<Integer, GameSession>();
	
	// thread-safe singleton implementation
    private static GameSessionManager manager = new GameSessionManager();     
    private GameSessionManager(){		
    	initAllGameSession(GAME_SESSION_COUNT);
	} 	    
    public static GameSessionManager getInstance() { 
    	return manager; 
    } 
	
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
	
	private boolean isForFree(int count){
		return (count >= 0);
	}
	
	private boolean isForCandidate(int count){
		return (count >= 1 && count <=4);
	}

//	private boolean isForCandidate2(int count){
//		return (count >= 2 && count<GameSessionUserManager.MAX_USER_PER_SESSION);
//	}
	
	private boolean isForFull(int count){
		return (count >= GameSessionUserManager.MAX_USER_PER_SESSION);
	}

	private int getSessionFromSet(Set<Integer> set, Set<Integer> excludeSessionSet){
		int sessionId = -1;
		if (set.isEmpty())
			return -1;

		Iterator<Integer> iterSet = set.iterator();
		if (iterSet == null || !iterSet.hasNext())
			return -1;					
		
		if (excludeSessionSet == null){
			sessionId = iterSet.next().intValue();
			return sessionId;
		}		
		
		Set<Integer> diffSet = new HashSet<Integer>();
		diffSet.addAll(set);
		diffSet.removeAll(excludeSessionSet);

		Iterator<Integer> iter = diffSet.iterator();
		while (iter != null && iter.hasNext()){
			sessionId = iter.next().intValue();
			if (!playSet.contains(sessionId)){
				return sessionId;
			}
		}
		
		return -1;
	}
	
	public int allocGameSessionForUser(String userId, String nickName, String avatar, boolean gender, 
			Channel channel, Set<Integer> excludeSessionSet) {		
		int sessionId = NO_SESSION_MATCH_FOR_USER;
		synchronized(sessionUserLock){
			
			ConcurrentHashSet<Integer> currentSet = null;
			
			sessionId = getSessionFromSet(candidateSet, excludeSessionSet);
			if (sessionId != -1){
				GameLog.info(sessionId, "alloc session, use candidate set");
				currentSet = candidateSet;
			}
			
//			sessionId = getSessionFromSet(candidateSet2, excludeSessionSet);
//			if (sessionId != -1){
//				GameLog.info(sessionId, "alloc session, use candidate set 2");
//				currentSet = candidateSet2;
//			}

			if (sessionId == -1){
				sessionId = getSessionFromSet(freeSet, excludeSessionSet);
				if (sessionId != -1){
					GameLog.info(sessionId, "alloc session, use free set");
					currentSet = freeSet;
				}
			}
						
			if (sessionId != -1){
				
				// add user into game session
				GameSession session = this.findGameSessionById(sessionId);
				int userCount = addUserIntoSession(userId, nickName, avatar, gender, channel, session);
				
				// adjust candidate and full set, also add user
				if (isForFull(userCount)){
					GameLog.info(sessionId, "alloc session, user count "+userCount+" reach max, remove from freeset/candidate set");
					freeSet.remove(sessionId);
					candidateSet.remove(sessionId);
					fullSet.add(sessionId);
				}
				else if (isForCandidate(userCount)){
					GameLog.info(sessionId, "alloc session, user count "+userCount+", move to candidate set");
					currentSet.remove(sessionId);
					candidateSet.add(sessionId);
				}
//				else if (isForCandidate2(userCount)){
//					GameLog.info(sessionId, "alloc session, user count "+userCount+", move to candidate set 2");
//					currentSet.remove(sessionId);
//					candidateSet2.add(sessionId);					
//				}
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
//			candidateSet2.remove(sessionId);
			freeSet.remove(sessionId);
			playSet.add(sessionId);
		}
	}
	
	public void adjustSessionSetForTurnComplete(GameSession session){
		
		// TODO add log
		synchronized(sessionUserLock){
			int sessionId = session.getSessionId();
			int userCount = sessionUserManager.getSessionUserCount(sessionId);
			
			playSet.remove(sessionId);
			
			// adjust candidate and full set, also add user
			if (isForFull(userCount)){
				fullSet.add(sessionId);
			}
			else if (isForCandidate(userCount)){
				candidateSet.add(sessionId);
			}				
//			else if (isForCandidate2(userCount)){
//				candidateSet2.add(sessionId);
//			}				
			else{
				freeSet.add(sessionId);
			}
		}		
	}
	
	public void adjustSessionSet(GameSession session) {
		synchronized(sessionUserLock){
			
			int sessionId = session.getSessionId();
			int userCount = sessionUserManager.getSessionUserCount(sessionId);
			
			boolean isSessionPlaying = playSet.contains(sessionId);			
			
			if (isForFull(userCount)){
				candidateSet.remove(sessionId);
//				candidateSet2.remove(sessionId);
				freeSet.remove(sessionId);
				fullSet.add(sessionId);
			}
			else if (isForCandidate(userCount)){ 
//				candidateSet2.remove(sessionId);
				freeSet.remove(sessionId);
				fullSet.remove(sessionId);
				if (!isSessionPlaying)
					candidateSet.add(sessionId);
			}
//			else if (isForCandidate2(userCount)){ 
//				candidateSet.remove(sessionId);
//				freeSet.remove(sessionId);
//				fullSet.remove(sessionId);
//				if (!isSessionPlaying)
//					candidateSet2.add(sessionId);
//			}
			else{
				candidateSet.remove(sessionId);
//				candidateSet2.remove(sessionId);
				fullSet.remove(sessionId);
				if (!isSessionPlaying)
					freeSet.add(sessionId);				
			}
		}
	}
	
	public void removeUserFromSession(String userId, GameSession session){
			sessionUserManager.removeUserFromSession(userId, session.getSessionId());
			adjustSessionSet(session);
			UserManager.getInstance().removeOnlineUserById(userId);
	}
	
	private int addUserIntoSession(String userId, String nickName, String avatar, boolean gender, Channel channel, GameSession session){
		User user = new User(userId, nickName, avatar, gender, channel, session.getSessionId());
		return sessionUserManager.addUserIntoSession(user, session);
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
		// TODO add log here
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
				else {
					// user is the first user
					int size = userList.size();
					if (size <= 1){
						session.setCurrentPlayUser(null);
					}
					else{
						session.setCurrentPlayUser(userList.get(index+1));
					}
				}
				break;
			}
			index ++;
		}
		
		if (userFound){
			
		}
	}
	
//	public void scheduleTimeOutOnSession(final GameSession session, int timeOutSeconds){
//
//		Callable callable = new Callable(){
//			@Override
//			public Object call()  {
//				GameService.getInstance().fireTimeOutEvent(session);
//				return null;
//			}			
//		};
//		
//		ScheduledFuture future = scheduleService.schedule(callable, timeOutSeconds, TimeUnit.SECONDS);  
//		
//		session.setTimeOutFuture(future);
//	}

}
