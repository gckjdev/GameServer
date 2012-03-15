package com.orange.gameserver.hit.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;


public class GameManager {
	protected static final Logger logger = Logger.getLogger("GameSession");

	public static int NO_SESSION_MATCH_FOR_USER = -1; 
	public static int GAME_SESSION_COUNT = 10;

	
	// a map to store game session
	ConcurrentMap<Integer, GameSession> gameCollection = new ConcurrentHashMap<Integer, GameSession>();
	AtomicInteger roomNumber = new AtomicInteger(0);
	AtomicInteger sessionIdIndex = new AtomicInteger(0);
	
	// thread-safe singleton implementation
    private static GameManager manager = new GameManager();     
    private GameManager(){		
    	createNewDrawGameSession(GAME_SESSION_COUNT);
    	logger.info("<GameManager> init");
    	logger.info("hashMap:"+gameCollection);
	} 	    
    public static GameManager getInstance() { 
    	return manager; 
    } 
	
    public int getGameSessionSize () {
		return sessionIdIndex.get();
	}
    
	public GameSession createNewGameSession(String gameName, String userId) {
		return null;
	}

	public DrawGameSession createNewDrawGameSession(String userId) {
		int sessionId = sessionIdIndex.incrementAndGet();	
		String roomName = roomNumber.incrementAndGet() + "";
		DrawGameSession session = new DrawGameSession(sessionId, roomName, userId);
		gameCollection.put(Integer.valueOf(sessionId), session);				
		return session;
	}
	
	
	public void createNewDrawGameSession(int count)
	{
		for (int i = 0; i < count; i++) {
			int sessionId = sessionIdIndex.incrementAndGet();	
			String roomName = roomNumber.incrementAndGet() + "";
			DrawGameSession session = new DrawGameSession(sessionId, roomName, null);
			gameCollection.put(Integer.valueOf(sessionId), session);
		}
	}
	
	public int allocNewGameSessionId() {
		return sessionIdIndex.incrementAndGet();
	}
	
	public GameSession findGameSessionById(int id) {
		Integer key = Integer.valueOf(id);
		if (!gameCollection.containsKey(key))
			return null;
		
		return gameCollection.get(key);		
	}
	
	public int matchGameForUser(String userId) {
		
		
		return NO_SESSION_MATCH_FOR_USER;
	}

}
