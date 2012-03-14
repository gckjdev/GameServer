package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GamePlayingState extends CommonGameState {

	public GamePlayingState(Object stateId) {
		super(stateId);
	}

	
	@Override
	public void handleEvent(GameEvent event, GameSession session){
		
		
	}
}
