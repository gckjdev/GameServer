package com.orange.gameserver.hit.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.hit.server.GameWorkerThread;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class HandlerUtils {

	protected static final Logger logger = Logger.getLogger(HandlerUtils.class.getName());

	public static void sendResponse(GameEvent gameEvent, GameMessage response) {
		if (gameEvent == null || response == null || gameEvent.getChannel() == null)
			return;
		
		logger.info(String.format("[%08X] [SEND] %s", response.getSessionId(), response.toString()));
		gameEvent.getChannel().write(response);
	}


	public static GameCommandType getResponseCommandByRequest(GameCommandType requestCommand){
		return GameCommandType.valueOf(requestCommand.getNumber() + 1);
	}
	
	public static void sendErrorResponse(GameEvent gameEvent, GameResultCode resultCode){
		
		GameMessage request = gameEvent.getMessage();
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		sendResponse(gameEvent, response);		
	}
}
