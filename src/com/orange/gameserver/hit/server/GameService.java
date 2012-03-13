package com.orange.gameserver.hit.server;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.UserAtGame;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.manager.UserAtGameManager;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameService {
	
	protected static final Logger logger = Logger.getLogger(GameService.class.getName());

	
	ConcurrentHashMap<Integer, GameWorkerThread> workerThreads = new ConcurrentHashMap<Integer, GameWorkerThread>();
	int numberOfWorkerThread = 20;
	
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
    
  	public int matchGameForUser(String userId, String gameId) {
  		// TODO
		return -1;
	}

  	
	public int createGame(String userId, String nickName) {		
		GameSession gameSession =  gameManager.createNewDrawGameSession(userId);		
		UserAtGame userAtGame = userManager.userLogin(userId, nickName);		
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
}
