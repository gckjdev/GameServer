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
		if (userId == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (session == null)
			return GameResultCode.ERROR_SESSIONID_NULL;
		
		if (session.isStart())
			return GameResultCode.ERROR_SESSION_ALREADY_START;
		
		if (!session.canUserStartGame(userId))
			return GameResultCode.ERROR_USER_CANNOT_START_GAME;		
		
		return GameResultCode.SUCCESS;
	}

	public static void handleStartGameRequest(GameEvent gameEvent,
			GameSession session) {
		
		// set current play user id and next play user id
		session.chooseNewPlayUser();		
		
		// start game
		session.startGame();
		
		// broast to all users in the session
		GameNotification.broadcastGameStartNotification(session, gameEvent);
	}

	
	public static GameResultCode validateSendDrawDataRequest(GameEvent gameEvent, GameSession session) {
		String userId = gameEvent.getMessage().getUserId();
		if (userId == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (session == null)
			return GameResultCode.ERROR_SESSIONID_NULL;
		
		if (!session.isStart())
			return GameResultCode.ERROR_SESSION_NOT_START;
		
		if (!gameEvent.getMessage().hasSendDrawDataRequest())
			return GameResultCode.ERROR_NO_DRAW_DATA;
		
		return GameResultCode.SUCCESS;
	}

	public static void handleSendDrawDataRequest(GameEvent gameEvent,
			GameSession session) {
		
		// broast draw data to all other users in the session
		GameNotification.broadcastDrawDataNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}

	public static void handleCleanDrawRequest(GameEvent gameEvent,
			GameSession session) {
		// broast draw data to all other users in the session
		GameNotification.broadcastCleanDrawNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}

	public static void hanndleChannelDisconnect(GameEvent gameEvent,
			GameSession session) {
		
		GameMessage message = gameEvent.getMessage();
		
		// remove user in session
		session.removeUser(message.getUserId());
		
		// broadcast user exit message
		GameNotification.broadcastUserQuitNotification(session, message.getUserId(), gameEvent);
	}
}
