package com.orange.gameserver.hit.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.orange.gameserver.hit.dao.DrawGameSession;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.UserAtGame;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.manager.UserAtGameManager;

public class GameService {
	
	GameManager gameManager = GameManager.getInstance();
	UserAtGameManager userManager = UserAtGameManager.getInstance();
	
	// thread-safe singleton implementation
    private static GameService defaultService = new GameService();     
    private GameService(){		
	} 	    
    public static GameService getInstance() { 
    	return defaultService; 
    } 
	
//	public void handleNewGameRequest(NewGameRequest request){
//
//	}
    
  	public GameSession matchGameForUser(String userId, String gameId) {
		return null;
	}

	public GameSession createGame(String userId, String nickName) {		
		GameSession gameSession =  gameManager.createNewDrawGameSession(userId);		
		UserAtGame userAtGame = userManager.userLogin(userId, nickName);		
		gameSession.addUser(userAtGame);
		return gameSession;
	}
}
