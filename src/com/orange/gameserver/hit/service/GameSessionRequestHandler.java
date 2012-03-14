package com.orange.gameserver.hit.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameSessionRequestHandler extends AbstractRequestHandler {

	public GameSessionRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message) {
		gameService.dispatchEvent(this.toGameEvent(gameMessage));
	}


	public static GameResultCode validateStartGameRequest(GameEvent gameEvent, GameSession session) {
		String userId = gameEvent.getMessage().getUserId();
		if (userId == null || session == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (!session.canUserStartGame(userId))
			return GameResultCode.ERROR_USER_CANNOT_START_GAME;		
		
		return GameResultCode.SUCCESS;
	}

}
