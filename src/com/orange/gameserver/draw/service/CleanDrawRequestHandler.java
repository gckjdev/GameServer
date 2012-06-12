package com.orange.gameserver.draw.service;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class CleanDrawRequestHandler extends AbstractRequestHandler  {

	public CleanDrawRequestHandler(GameEvent event, GameSession session) {
		super(event, session);
	}

	@Override
	public void handleRequest(GameMessage message) {

		if (session != null){
			session.appendCleanDrawAction();
		}
		
		// broast draw data to all other users in the session
		GameNotification.broadcastCleanDrawNotification(session, message.getUserId());		
	}

	
	
}
