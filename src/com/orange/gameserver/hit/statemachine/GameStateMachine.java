package com.orange.gameserver.hit.statemachine;

import com.orange.common.statemachine.StateMachine;
import com.orange.gameserver.hit.manager.GameManager;

public class GameStateMachine extends StateMachine {

	GameManager gameManager;
	
	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}

}
