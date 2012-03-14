package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;

public class GameFinishState extends CommonGameState {

	public final static GameFinishState defaultState = new GameFinishState(GameStateKey.FINISH);
	
	public GameFinishState(Object stateId) {
		super(stateId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleEvent(GameEvent event, GameSession session){
	}
}
