package com.orange.gameserver.hit.service;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.gameserver.hit.statemachine.game.GameEventKey;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.model.GameBasicProtos;

public class JoinGameRequestHandler extends AbstractRequestHandler {

	public JoinGameRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage request) {
		
		String userId = request.getJoinGameRequest().getUserId();
		String gameId = request.getJoinGameRequest().getGameId();
		String nickName = request.getJoinGameRequest().getNickName();
		
		this.log("user id = " + userId); 
		
		/*
		// match a game for user to join			
		GameSession gameSession = gameService.matchGameForUser(userId, gameId);
		if (gameSession == null){
			// if cannot match a game for user, then try to create a new one
			gameSession = gameService.createGame(userId, nickName);
		}
		
		if (gameSession == null){
			sendErrorResponse(GameMessageProtos.GameResultCode.ERROR_JOIN_GAME, request);
			return;
		}
		*/
		
		int gameSessionId = gameService.matchGameForUser(userId, gameId);
		if (gameSessionId == -1){
			gameSessionId = gameService.createGame(userId, nickName);
		}
		
		GameEvent gameEvent = new GameEvent(GameEventKey.EVENT_USER_JOIN_GAME, 
				gameSessionId, request, messageEvent.getChannel());

		gameService.dispatchEvent(gameEvent);
		
		
	}

	public static void handleJoinGameRequest(GameEvent gameEvent,
			GameSession gameSession) {

		GameMessage request = gameEvent.getMessage();
				
		String userId = request.getJoinGameRequest().getUserId();
		String nickName = request.getJoinGameRequest().getNickName();
		
		boolean result = gameSession.addUser(userId, nickName);
		
		List<GameBasicProtos.PBGameUser> pbGameUserList = gameSession.usersToPBUsers();
		
		GameBasicProtos.PBGameSession gameSessionData = GameBasicProtos.PBGameSession.newBuilder()		
										.setCreateBy(gameSession.getCreateBy())
										.setCreateTime((int)gameSession.getCreateDate().getTime()/1000)
										.setGameId("DrawGame")
										.setHost(gameSession.getHost())
										.setName(gameSession.getName())
										.setSessionId(gameSession.getSessionId())
										.addAllUsers(pbGameUserList)
										.build();

		GameMessageProtos.JoinGameResponse joinGameResponse = GameMessageProtos.JoinGameResponse.newBuilder()
										.setGameSession(gameSessionData)
										.build();
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
					.setCommand(GameCommandType.JOIN_GAME_RESPONSE)
					.setMessageId(request.getMessageId())
					.setResultCode(GameMessageProtos.GameResultCode.SUCCESS)
					.setJoinGameResponse(joinGameResponse)
					.build();

		HandlerUtils.sendResponse(gameEvent, response);
	}

	

	

}
