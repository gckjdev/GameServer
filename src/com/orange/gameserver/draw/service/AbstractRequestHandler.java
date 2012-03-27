package com.orange.gameserver.draw.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.manager.GameManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.statemachine.game.GameEventKey;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;

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
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();
	
		logger.info(String.format("[%08X] [SEND] %s", response.getSessionId(), response.toString()));
		messageEvent.getChannel().write(response);
	}
	
	public void printRequest(GameMessage request){
		logger.info(String.format("[%08X] [RECV] %s", request.getMessageId(), request.toString()));	
	}
	
	public void log(String message){
		logger.info(String.format("[%08X] %s", gameMessage.getMessageId(), message));
	}
	
	public GameEvent toGameEvent(GameMessage gameMessage){
		return new GameEvent(
				gameMessage.getCommand(), 
				(int)gameMessage.getSessionId(), 
				gameMessage, 
				messageEvent.getChannel());
	}

}
