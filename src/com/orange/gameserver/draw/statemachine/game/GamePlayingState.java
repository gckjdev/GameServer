package com.orange.gameserver.draw.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GamePlayingState extends CommonGameState {

	public GamePlayingState(Object stateId) {
		super(stateId);
	}

	
	@Override
	public void handleEvent(GameEvent event, GameSession session){		
		session.startGame();
	}
}
