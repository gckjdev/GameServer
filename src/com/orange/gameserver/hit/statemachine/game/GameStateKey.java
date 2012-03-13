package com.orange.gameserver.hit.statemachine.game;

public enum GameStateKey {
	CREATE (0),
	WAITING (1),
	PLAYING (2),
	SUSPEND (3),
	FINISH (4);
	
	final int value;
	
	GameStateKey(int value){
		this.value = value;
	}
}
