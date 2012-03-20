package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GameWaitingState extends CommonGameState {

	public GameWaitingState(Object stateId) {
		super(stateId);
	}

	@Override
	public void handleEvent(GameEvent event, GameSession session){
		session.resetExpireTimer();
		GameManager.getInstance().adjustSessionSetForWaiting(session); // TODO so so performance here...		
	}
}
