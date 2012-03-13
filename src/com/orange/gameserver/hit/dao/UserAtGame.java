package com.orange.gameserver.hit.dao;

public class UserAtGame {

	String userId;
	String nickName;
	
	GameSession gameSession;
	
	UserAtGameState state = UserAtGameState.CREATE;
	
	public UserAtGame(String userId, String nickName) {
		this.userId = userId;
		this.nickName = nickName;
	}	
}
