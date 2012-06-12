package com.orange.gameserver.draw.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class StartGameRequestHandler extends AbstractRequestHandler {

	public StartGameRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	public StartGameRequestHandler(GameEvent event, GameSession session) {
		super(event, session);
	}

	private GameResultCode validateStartGameRequest(GameEvent gameEvent, GameSession session) {
		String userId = gameEvent.getMessage().getUserId();
		if (userId == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (session == null)
			return GameResultCode.ERROR_SESSIONID_NULL;
		
		if (session.isStart())
			return GameResultCode.ERROR_SESSION_ALREADY_START;
		
		return GameResultCode.SUCCESS;
	}

	
	@Override
	public void handleRequest(GameMessage message) {
		
		// TODO call validateStartGameRequest
		
		GameEvent gameEvent = toGameEvent(message);
		GameSession session = sessionManager.findGameSessionById((int) message.getSessionId());
		
		GameSessionManager.getInstance().adjustSessionSetForPlaying(session); // adjust set so that it's not allowed to join
		sessionUserManager.setUserPlaying(session);

		// start game
//		session.startGame();
//		session.clearStartExpireTimer();
		
		// send reponse
		GameMessageProtos.StartGameResponse gameResponse = GameMessageProtos.StartGameResponse.newBuilder()
			.setCurrentPlayUserId(session.getCurrentPlayUserId())
			.setNextPlayUserId("")
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
		
		// drive state machine running
		GameEvent stateMachineEvent = new GameEvent(
				GameCommandType.LOCAL_START_GAME, 
				session.getSessionId(), 
				gameEvent.getMessage(), 
				channel);
		
		gameService.dispatchEvent(stateMachineEvent);				
		
	}

}
