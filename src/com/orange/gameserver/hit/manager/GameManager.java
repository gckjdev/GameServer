package com.orange.gameserver.hit.manager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.statemachine.GameStateMachine;
import com.orange.gameserver.hit.statemachine.GameStateMachineBuilder;

public class GameManager {
	
	// a map to store game session
	ConcurrentMap<String, GameSession> gameCollection = new ConcurrentHashMap<String, GameSession>();
	GameStateMachine gameStateMachine;
	
	// thread-safe singleton implementation
    private static GameManager manager = new GameManager();     
    private GameManager(){		
	} 	    
    public static GameManager getInstance() { 
    	return manager; 
    } 
	
	public GameSession createNewGameSession(String gameName, String userId) {
		String gameId = UUID.randomUUID().toString();		
		GameSession session = new GameSession(gameId, gameName, userId);
		gameCollection.put(gameId, session);		
		return session;
	}

	public GameStateMachine getGameStateMachine() {
		return this.gameStateMachine;
	}
	

}
