package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.message.GameMessageProtos.GameCommandType;

public class GameWaitingState extends State {

	public GameWaitingState(Object stateId) {
		super(stateId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void enterAction(Event event, Object context) {
		GameEvent gameEvent = (GameEvent)event;
		GameSession session = (GameSession)context;
		
		if (gameEvent.getMessage().getCommand() == GameCommandType.JOIN_GAME_REQUEST){
			JoinGameRequestHandler.handleJoinGameRequest(gameEvent, session);
		}
		
		
	}
}
