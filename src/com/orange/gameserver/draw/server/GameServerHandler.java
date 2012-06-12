package com.orange.gameserver.draw.server;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.gameserver.draw.manager.ChannelUserManager;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.service.AbstractRequestHandler;
import com.orange.gameserver.draw.service.GameSessionRequestHandler;
import com.orange.gameserver.draw.service.HandlerUtils;
import com.orange.gameserver.draw.service.JoinGameRequestHandler;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;



public class GameServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(GameServerHandler.class
			.getName()); 
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e){
		
		try {
			logger.debug(e.toString());
			super.handleUpstream(ctx, e);
		} catch (Exception exception) {
			logger.error("<handleUpstream> catch unexpected exception at " + e.getChannel().toString() + ", cause=", exception.getCause());			
			ChannelUserManager.getInstance().processDisconnectChannel(e.getChannel());
		}

	}
	
	public GameEvent toGameEvent(GameMessage gameMessage, MessageEvent messageEvent){
		return new GameEvent(
				gameMessage.getCommand(), 
				(int)gameMessage.getSessionId(), 
				gameMessage, 
				messageEvent.getChannel());
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
				
		GameMessage message = (GameMessageProtos.GameMessage)e.getMessage();
		
		if (message.getCommand() == GameConstantsProtos.GameCommandType.KEEP_ALIVE_REQUEST){
			GameLog.info((int)message.getSessionId(), "recv KEEP ALIVE for user " + message.getUserId());

			// if receive some message, then keep user not time out...
			// rem by Benson to avoid user dead leave, to be improved
//			ChannelUserManager.getInstance().resetUserTimeOut(e.getChannel());
			return;
		}				
		
		AbstractRequestHandler handler = null;
		
		
		if (message.getCommand() == GameConstantsProtos.GameCommandType.JOIN_GAME_REQUEST){
			handler = new JoinGameRequestHandler(e);
			handler.handleRequest(message);			
		}
		else if (message.hasSessionId()){
			handler = new GameSessionRequestHandler(e);
			GameService.getInstance().dispatchEvent(toGameEvent(message, e));
		}
		
//		if (handler == null){	
//			sendErrorResponse(e, message, GameConstantsProtos.GameResultCode.ERROR_SYSTEM_HANDLER_NOT_FOUND);
//			return;
//		}
		
		// if receive some message, then keep user not time out...
		ChannelUserManager.getInstance().resetUserTimeOut(e.getChannel());		
	}
	
	public void sendErrorResponse(MessageEvent messageEvent, GameMessage request, GameResultCode resultCode){
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		GameLog.info((int)response.getSessionId(), resultCode.toString());
		messageEvent.getChannel().write(response);
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("<exceptionCaught> catch unexpected exception at " + e.getChannel().toString() + ", cause=", e.getCause());
		ChannelUserManager.getInstance().processDisconnectChannel(e.getChannel());
	}				
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){

		ChannelUserManager.getInstance().processDisconnectChannel(e.getChannel());
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx,
            ChannelStateEvent e){

		ChannelUserManager.getInstance().processDisconnectChannel(e.getChannel());		
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		ChannelUserManager.getInstance().addChannel(e.getChannel());
		
	}
	
}
