package com.orange.gameserver.draw.statemachine.game;

// useless now, keep it before it dies...
public enum GameEventKey {

	EVENT_USER_JOIN_GAME (11); 
	
	final int value;
	
	GameEventKey(int value){
		this.value = value;
	}
}
