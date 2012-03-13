package com.orange.gameserver.hit.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;
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
		String gameName = request.getJoinGameRequest().getGameId();
		
		this.log("user id = " + userId); 
		
		// create new game
		GameSession newGameSession = gameManager.createNewGameSession(gameName, userId);						
		if (newGameSession == null){
			sendErrorResponse(GameMessageProtos.GameResultCode.ERROR_JOIN_GAME, request);
			return;
		}
		
		// start new game session state machine handling		
//		newGameSession.handleMessage();
		
		// send back new game response
		// TODO need detail implementation here
		GameBasicProtos.PBGameSession gameSessionData = GameBasicProtos.PBGameSession.newBuilder()
										.setCreateBy("test user")
										.setCreateTime((int)(System.currentTimeMillis()/1000))
										.setGameId("test game id")
										.setHost("host")
										.setName("test user name")
										.setSessionId(1234)
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

		sendResponse(response);				
	}

	

}
