package com.orange.gameserver.draw.service;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.server.GameWorkerThread;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class HandlerUtils {

//	protected static final Logger logger = Logger.getLogger(HandlerUtils.class.getName());

	public static void safeWrite(Channel channel, GameMessage message){
		if (channel.isConnected() && channel.isWritable()){
			channel.write(message);
		}
	}
	
	public static void logSendMessage(GameMessage message){
		GameLog.info((int)message.getSessionId(), message.getCommand().toString().concat("==>").concat(
				message.getToUserId()).concat(" ").concat(message.getResultCode().toString()));
		
	}
	
	public static void sendMessage(GameMessage message, Channel channel) {
		if (message == null || channel == null)
			return;
		
		GameLog.debug((int)message.getSessionId(), message.toString());
		logSendMessage(message);
		safeWrite(channel, message);
	}
	
	public static void sendMessage(GameEvent gameEvent, GameMessage message, Channel channel) {
		if (gameEvent == null || message == null || channel == null)
			return;
		
		GameLog.debug((int)message.getSessionId(), message.toString());
		logSendMessage(message);
		safeWrite(channel, message);
	}
	
	public static void sendResponse(GameEvent gameEvent, GameMessage response) {
		if (gameEvent == null || response == null || gameEvent.getChannel() == null)
			return;
		
		GameLog.debug((int)response.getSessionId(), response.toString());
		logSendMessage(response);
		safeWrite(gameEvent.getChannel(), response);

	}
	
	public static GameCommandType getResponseCommandByRequest(GameCommandType requestCommand){
		return GameCommandType.valueOf(requestCommand.getNumber() + 1);
	}
	
	public static void sendErrorResponse(GameEvent gameEvent, GameResultCode resultCode){
		
		GameMessage request = gameEvent.getMessage();
		
		GameCommandType command = HandlerUtils.getResponseCommandByRequest(request.getCommand());
		if (command == null)
			return;
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(command)
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		sendResponse(gameEvent, response);		
	}
	
	public static void sendErrorResponse(GameMessage request, GameResultCode resultCode, Channel channel){
		
		if (request == null || channel == null)
			return;
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		GameLog.debug((int)response.getSessionId(), response.toString());
		logSendMessage(response);
		safeWrite(channel, response);
	}
}
