package com.orange.gameserver.hit.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;

import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.NewGameRequest;

public class GameService {
	
	GameManager gameManager = GameManager.getInstance();
	
	// thread-safe singleton implementation
    private static GameService defaultService = new GameService();     
    private GameService(){		
	} 	    
    public static GameService getInstance() { 
    	return defaultService; 
    } 
	
	public void handleNewGameRequest(NewGameRequest request){

	}
}
