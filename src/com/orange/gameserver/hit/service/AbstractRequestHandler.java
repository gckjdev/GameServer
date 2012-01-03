package com.orange.gameserver.hit.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.statemachine.GameStateMachine;
import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.GameRequest;
import com.orange.network.game.protocol.GameProtos.GameResponse;

public abstract class AbstractRequestHandler {
	
	protected static final Logger logger = Logger.getLogger(AbstractRequestHandler.class.getName());
	
	GameManager gameManager;	// use for game session management
	GameStateMachine gameStateMachine;
	MessageEvent messageEvent;	// use to get channel and send back response
	GameRequest gameRequest;
	
	public AbstractRequestHandler(GameManager gameManager, MessageEvent messageEvent) {
		this.gameManager = gameManager;
		this.messageEvent = messageEvent;		
		this.gameRequest = (GameRequest)messageEvent.getMessage();
		this.gameStateMachine = gameManager.getGameStateMachine();
	}

	public abstract void handleRequest(GameRequest request);
	
	public void sendResponse(GameResponse response){
		if (messageEvent == null)
			return;

		logger.info(String.format("[%08X] [SEND] %s", response.getId(), response.toString()));
		messageEvent.getChannel().write(response);
	}
	
	public void sendErrorResponse(GameProtos.ResultCodeType resultCode, GameRequest request){
		GameProtos.GameResponse response = GameProtos.GameResponse.newBuilder().
			setId(request.getId()).
			setResultCode(resultCode).						
			build();

		sendResponse(response);
	}
	
	public void printRequest(GameRequest request){
		logger.info(String.format("[%08X] [RECV] %s", request.getId(), request.toString()));	
	}
	
	public void log(String message){
		logger.info(String.format("[%08X] %s", gameRequest.getId(), message));
	}
}
