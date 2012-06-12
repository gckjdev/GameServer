package com.orange.gameserver.draw.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GameStartState extends CommonGameState {

	public final static GameStartState defaultState = new GameStartState(GameStateKey.CREATE);
	
	public GameStartState(Object stateId) {
		super(stateId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleEvent(GameEvent event, GameSession session){
//		session.resetGame();
	}
}
