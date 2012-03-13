package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.State;

public class GameFinishState extends State {

	public final static GameFinishState defaultState = new GameFinishState(GameStateKey.FINISH);
	
	public GameFinishState(Object stateId) {
		super(stateId);
		// TODO Auto-generated constructor stub
	}

}
