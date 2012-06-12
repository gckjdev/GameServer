package com.orange.gameserver.draw.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.statemachine.game.GameEventKey;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;

public abstract class AbstractRequestHandler {
	
	protected static final Logger logger = Logger.getLogger(AbstractRequestHandler.class.getName());
	
	
	protected static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	protected static final GameSessionManager sessionManager = GameSessionManager.getInstance();
	protected static final UserManager userManager = UserManager.getInstance();
	
	GameSessionManager gameManager = GameSessionManager.getInstance();	// use for game session management
//	MessageEvent messageEvent;	// use to get channel and send back response
	final Channel channel;
	final GameSession session;
	final GameMessage gameMessage;
	GameService gameService = GameService.getInstance();
	
	public AbstractRequestHandler(MessageEvent messageEvent) {
//		this.messageEvent = messageEvent;
		this.channel = messageEvent.getChannel();
		this.gameMessage = (GameMessage)messageEvent.getMessage();
		this.session = null;
	}

	public AbstractRequestHandler(GameEvent event, GameSession session) {
//		this.messageEvent = messageEvent;
		this.channel = event.getChannel();
		this.gameMessage = event.getMessage();
		this.session = session;
	}
	
	public abstract void handleRequest(GameMessage message);
	
	public void sendResponse(GameMessage response){
		if (channel == null || response == null)
			return;

		GameLog.debug((int)response.getSessionId(), "[SEND] ", response.toString());
		GameLog.info((int)response.getSessionId(), "[SEND] ", 
				response.getCommand().toString(), 
				response.getResultCode().toString());
		
		channel.write(response);
	}
	
	public void sendErrorResponse(GameResultCode resultCode, GameMessage request){
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();
	
		GameLog.info((int)response.getSessionId(), "[SEND] ", 
				response.getCommand().toString(), response.getResultCode().toString());
		channel.write(response);
	}
	
//	public void printRequest(GameMessage request){
//		logger.info(String.format("[%08X] [RECV] %s", request.getMessageId(), request.toString()));	
//	}
//	
//	public void log(String message){
//		logger.info(String.format("[%08X] %s", gameMessage.getMessageId(), message));
//	}
	
	public GameEvent toGameEvent(GameMessage gameMessage){
		return new GameEvent(
				gameMessage.getCommand(), 
				(int)gameMessage.getSessionId(), 
				gameMessage, 
				channel);
	}

}
