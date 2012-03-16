package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GameStartState extends CommonGameState {

	public final static GameStartState defaultState = new GameStartState(GameStateKey.CREATE);
	
	public GameStartState(Object stateId) {
		super(stateId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleEvent(GameEvent event, GameSession session){
		session.resetGame();
	}
}
