package com.orange.gameserver.hit.server;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.User;
import com.orange.gameserver.hit.dao.UserAtGame;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.manager.GameSessionManager;
import com.orange.gameserver.hit.manager.UserAtGameManager;
import com.orange.gameserver.hit.manager.UserManager;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

import com.orange.gameserver.hit.dao.GameSession;
import com.sun.java_cup.internal.runtime.Symbol;
public class GameService {
	
	protected static final Logger logger = Logger.getLogger(GameService.class.getName());

	
	ConcurrentHashMap<Integer, GameWorkerThread> workerThreads = new ConcurrentHashMap<Integer, GameWorkerThread>();
	int numberOfWorkerThread = 20;
	
	AtomicInteger messageIdIndex = new AtomicInteger(0);
	
	GameManager gameManager = GameManager.getInstance();
	UserAtGameManager userManager = UserAtGameManager.getInstance();
	
	// thread-safe singleton implementation
    private static GameService defaultService = new GameService();     
    private GameService(){		
	} 	    
    public static GameService getInstance() { 
    	return defaultService; 
    } 
	
	public ConcurrentHashMap<Integer, GameWorkerThread> createWorkerThreads(int numberOfThread){
		numberOfWorkerThread = numberOfThread;
		for (int i=0; i<numberOfThread; i++){
			GameWorkerThread worker = new GameWorkerThread();
			workerThreads.put(Integer.valueOf(i), worker);
			worker.threadHashKey = i;
			worker.start();
			logger.info("Create & Start Worker Thread " + i);
		}
		
		return workerThreads;
	}
    
	
	private int getMatchedSessionId(User user, boolean useRecentSessions ) {
		GameSessionManager gameSessionManager = GameSessionManager.getInstance();
		GameManager gameManager = GameManager.getInstance();
		int joinSessionId = -1;
		if (useRecentSessions) {
			int freeSessionIdInList = -1;
			//if can find the candidate session for the session id in the recent session list
			for(Integer sessionId : user.getRecentGameSessionList())
			{
				int sessionSymbol =  gameSessionManager.getGameSessionSetSymbolById(sessionId);
				if (sessionSymbol == GameSessionManager.SESSION_SET_CANDIDATE) {
					joinSessionId = sessionId.intValue();
					break;
				}else if(sessionSymbol == GameSessionManager.SESSION_SET_FREE && freeSessionIdInList == -1){
					freeSessionIdInList = sessionId.intValue();
				}
			}
			
			//if can not find the candidate session for the session id in the recent session list, random a free session
			if (joinSessionId == -1) {
				//if can find a free session in the recent list, the the join session is the free session
				//else find a free session in the free set
				if (freeSessionIdInList != -1) {
					joinSessionId = freeSessionIdInList;
				}else{
					joinSessionId = gameSessionManager.getRandGameSessionId(GameSessionManager.SESSION_SET_FREE);
				}
			}
			
		}else{
			//if don't use the recent list
			//find a candidate session not in the list
			joinSessionId = gameSessionManager.getCandidateGameSessionNotInList(user.getRecentGameSessionList());
			if (joinSessionId == -1) {
				joinSessionId = gameSessionManager.getRandGameSessionId(gameSessionManager.SESSION_SET_FREE);
			}
		}
		if (joinSessionId != -1) {
			gameSessionManager.adjustGameSession(joinSessionId);
		}
		return joinSessionId;
	}
	
  	public int matchGameForUser(String userId, String gameId) {
  		boolean useRecentSessions = true;
  		if (userId != null) {
  			UserManager userManager = UserManager.getInstance();
  			User user = userManager.findUserById(userId);
  			if (user != null) {
				return getMatchedSessionId(user, useRecentSessions);
			}
		}
		return -1;
	}

  	
	public int createGame(String userId, String nickName, Channel channel) {		
		GameSession gameSession =  gameManager.createNewDrawGameSession(userId);		
		UserAtGame userAtGame = userManager.userLogin(userId, nickName, channel);		
		gameSession.addUser(userAtGame);
		return gameSession.getSessionId();
	}

	/*
	public int allocNewGameSessionId() {
		return gameManager.allocNewGameSessionId();
	}
	*/
	
	public void dispatchEvent(GameEvent gameEvent) {
		long assignWorkerThreadIndex = hash(gameEvent.getTargetSession());
		Integer key = Integer.valueOf((int)assignWorkerThreadIndex);
		if (workerThreads.containsKey(key)){
			GameWorkerThread worker = workerThreads.get(key);
			worker.putEvent(gameEvent);
		}
	}

	private long hash(long targetSession) {
		return (targetSession + 31) % numberOfWorkerThread;
	}
	
	public void sendResponse(Channel channel, GameMessage response) {
		// TODO Auto-generated method stub
		
	}
	public int generateMessageId() {
		return messageIdIndex.incrementAndGet();
	}
}
