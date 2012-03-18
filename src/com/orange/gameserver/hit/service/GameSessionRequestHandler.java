package com.orange.gameserver.hit.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
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
		
		// send reponse
		GameMessageProtos.StartGameResponse gameResponse = GameMessageProtos.StartGameResponse.newBuilder()
			.setCurrentPlayUserId(session.getCurrentPlayUserId())
			.setNextPlayUserId(session.getNextPlayUserId())
			.build();
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.START_GAME_RESPONSE)
			.setMessageId(gameEvent.getMessage().getMessageId())
			.setResultCode(GameResultCode.SUCCESS)
			.setStartGameResponse(gameResponse)
			.build();
		HandlerUtils.sendResponse(gameEvent, response);
		
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
		
		if (session.isRoomEmpty()){
			// if there is no user, fire a finish message
			GameService.getInstance().fireAndDispatchEvent(GameCommandType.LOCAL_FINISH_GAME, 
					session.getSessionId(), null);
		}
		else{
			// broadcast user exit message to all other users
			GameNotification.broadcastUserQuitNotification(session, message.getUserId(), gameEvent);			
		}		
	}

	public static void hanndleFinishGame(GameEvent gameEvent,
			GameSession session) {
		session.resetGame();
	}
}
