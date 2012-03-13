package com.orange.gameserver.hit.statemachine.game;

public enum GameEventKey {

	EVENT_GAME_CREATED (1),	
	EVENT_USER_JOIN_GAME (11); 
	
	final int value;
	
	GameEventKey(int value){
		this.value = value;
	}
}
