package com.orange.gameserver.draw.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GameWaitingState extends CommonGameState {

	public GameWaitingState(Object stateId) {
		super(stateId);
	}

	@Override
	public void handleEvent(GameEvent event, GameSession session){
		session.waitForPlay();
//		GameSessionManager.getInstance().adjustSessionSetForWaiting(session); // TODO so so performance here...		
	}
}
