package com.orange.gameserver.hit.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.server.GameService;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.GameResultCode;

public abstract class AbstractRequestHandler {
	
	protected static final Logger logger = Logger.getLogger(AbstractRequestHandler.class.getName());
	
	GameManager gameManager = GameManager.getInstance();	// use for game session management
	MessageEvent messageEvent;	// use to get channel and send back response
	GameMessage gameMessage;
	GameService gameService = GameService.getInstance();
	
	public AbstractRequestHandler(MessageEvent messageEvent) {
		this.messageEvent = messageEvent;		
		this.gameMessage = (GameMessage)messageEvent.getMessage();
	}

	public abstract void handleRequest(GameMessage message);
	
	public void sendResponse(GameMessage response){
		if (messageEvent == null)
			return;

		logger.info(String.format("[%08X] [SEND] %s", response.getMessageId(), response.toString()));
		messageEvent.getChannel().write(response);
	}
	
	public void sendErrorResponse(GameResultCode resultCode, GameMessage request){
		GameMessage response = GameMessageProtos.GameMessage.newBuilder().
			setMessageId(request.getMessageId()).
			setResultCode(resultCode).						
			build();

		sendResponse(response);
	}
	
	public void printRequest(GameMessage request){
		logger.info(String.format("[%08X] [RECV] %s", request.getMessageId(), request.toString()));	
	}
	
	public void log(String message){
		logger.info(String.format("[%08X] %s", gameMessage.getMessageId(), message));
	}
}
