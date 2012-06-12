package com.orange.gameserver.draw.service;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class ChatRequestHandler extends AbstractRequestHandler {

	public ChatRequestHandler(GameEvent event, GameSession session) {
		super(event, session);
	}

	@Override
	public void handleRequest(GameMessage message) {
		GameChatRequest chatRequest = message.getChatRequest();
		if (chatRequest == null)
			return;				
		
		String fromUserId = message.getUserId();
		if (session.getCurrentPlayUserId().equals(fromUserId)){
			// fire draw user chat event
			gameService.fireAndDispatchEvent(GameCommandType.LOCAL_DRAW_USER_CHAT, 
					session.getSessionId(), fromUserId);
		}
		
		// broast draw data to all other users in the session
		GameNotification.broadcastChatNotification(session, message, fromUserId);
	}

}
