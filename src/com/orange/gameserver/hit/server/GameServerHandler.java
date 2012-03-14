package com.orange.gameserver.hit.server;

import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.gameserver.hit.manager.ChannelUserManager;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.service.AbstractRequestHandler;
import com.orange.gameserver.hit.service.GameSessionRequestHandler;
import com.orange.gameserver.hit.service.HandlerUtils;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;



public class GameServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(GameServerHandler.class
			.getName()); 
	
	private GameService gameService = GameService.getInstance();
	private GameManager gameManager = GameManager.getInstance();

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		logger.info(e.toString());
		if (e instanceof ChannelStateEvent) {
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
				
		GameMessage message = (GameMessageProtos.GameMessage)e.getMessage();
		
		AbstractRequestHandler handler = null;
		if (message.getCommand() == GameConstantsProtos.GameCommandType.JOIN_GAME_REQUEST){
			handler = new JoinGameRequestHandler(e);
		}				
		else if (message.hasSessionId()){
			handler = new GameSessionRequestHandler(e);
		}
		
		if (handler == null){	
			sendErrorResponse(e, message, GameConstantsProtos.GameResultCode.ERROR_SYSTEM_HANDLER_NOT_FOUND);
			return;
		}
		
		handler.handleRequest(message);			
	}
	
	public void sendErrorResponse(MessageEvent messageEvent, GameMessage request, GameResultCode resultCode){
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(HandlerUtils.getResponseCommandByRequest(request.getCommand()))
			.setMessageId(request.getMessageId())
			.setResultCode(resultCode)
			.build();

		logger.info(String.format("[%08X] [SEND] %s", response.getSessionId(), response.toString()));
		messageEvent.getChannel().write(response);
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("GameServerHandler catch unexpected exception .", e.getCause());
		e.getChannel().close();
	}
			
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.info("GameServerHandler channel disconnected");				
		ChannelUserManager.getInstance().addChannel(e.getChannel());
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.info("GameServerHandler channel connected");		
		
		// find all users related to the channel and post a message to game session that this user is quit
//		List<String> userList = 
		
		// remove channel
		ChannelUserManager.getInstance().removeChannel(e.getChannel());
	}
	
//	private static String toString(Continent c) {
//		return "" + c.name().charAt(0) + c.name().toLowerCase().substring(1);
//	}
}
