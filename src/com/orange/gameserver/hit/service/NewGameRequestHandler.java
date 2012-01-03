package com.orange.gameserver.hit.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.GameRequest;

public class NewGameRequestHandler extends AbstractRequestHandler {

	public NewGameRequestHandler(GameManager gamemanager, MessageEvent messageEvent) {
		super(gamemanager, messageEvent);
	}

	@Override
	public void handleRequest(GameRequest request) {
		
		String gameName = request.getNewGameCommand().getName();
		String userId = request.getNewGameCommand().getUserId();
		
		// create new game
		GameSession newGameSession = gameManager.createNewGameSession(gameName, userId);						
		if (newGameSession == null){
			sendErrorResponse(GameProtos.ResultCodeType.ERROR_CREATE_GAME, request);
			return;
		}
		
		// start new game session state machine handling		
		
		
		// send back new game response
		GameProtos.NewGameResponse newGameResponse = GameProtos.NewGameResponse.newBuilder()
										.setGame(newGameSession.toGame())
										.build();
		
		GameProtos.GameResponse response = GameProtos.GameResponse.newBuilder().
					setId(request.getId()).
					setResultCode(GameProtos.ResultCodeType.SUCCESS).						
					setNewGameResp(newGameResponse).
					build();

		sendResponse(response);				
	}

}
