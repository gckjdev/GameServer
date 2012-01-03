package com.orange.gameserver.hit.dao;

public class UserAtGame {

	String userId;
	GameSession gameSession;	
	UserAtGameState state = UserAtGameState.CREATE;
	
	public UserAtGame(String userId, GameSession gameSession) {
		this.userId = userId;
		this.gameSession = gameSession;
	}	
}
