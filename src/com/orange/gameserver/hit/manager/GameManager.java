package com.orange.gameserver.hit.manager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.statemachine.GameStateMachineBuilder;

public class GameManager {
	
	// a map to store game session
	ConcurrentMap<Integer, GameSession> gameCollection = new ConcurrentHashMap<Integer, GameSession>();
	AtomicInteger roomNumber = new AtomicInteger(0);
	AtomicInteger sessionIdIndex = new AtomicInteger(0);
	
	// thread-safe singleton implementation
    private static GameManager manager = new GameManager();     
    private GameManager(){		
	} 	    
    public static GameManager getInstance() { 
    	return manager; 
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
	

}
