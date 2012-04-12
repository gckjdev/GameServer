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
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.constants.GameConstantsProtos;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;



public class GameServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(GameServerHandler.class
			.getName()); 
	
	private GameService gameService = GameService.getInstance();
	private GameSessionManager gameManager = GameSessionManager.getInstance();

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		logger.debug(e.toString());
		super.handleUpstream(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
				
		GameMessage message = (GameMessageProtos.GameMessage)e.getMessage();
		
		AbstractRequestHandler handler = null;
		if (message.hasSessionId()){
			handler = new GameSessionRequestHandler(e);
		}
		else if (message.getCommand() == GameConstantsProtos.GameCommandType.JOIN_GAME_REQUEST){
			handler = new JoinGameRequestHandler(e);
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

		logger.debug(String.format("[%08X] [SEND] %s", response.getSessionId(), response.toString()));
		logger.info(String.format("[%08X] [SEND] error (%d)", response.getSessionId(), resultCode.toString()));
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
		logger.debug("GameServerHandler channel disconnected");		
		
		// find all users related to the channel and post a message to game session that this user is quit
		Channel channel = e.getChannel();		
		List<String> userIdList = ChannelUserManager.getInstance().findUsersInChannel(channel);
		for (String userId : userIdList){
			int sessionId = UserManager.getInstance().findGameSessionIdByUserId(userId);
			if (sessionId != -1){
				// fire event to the game session								
				gameService.fireAndDispatchEvent(GameCommandType.LOCAL_CHANNEL_DISCONNECT,
						sessionId, userId);
			}

			UserManager.getInstance().removeOnlineUserById(userId);
		}
		
		// remove channel
		ChannelUserManager.getInstance().removeChannel(e.getChannel());
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.debug("GameServerHandler channel connected");		
		ChannelUserManager.getInstance().addChannel(e.getChannel());
		
	}
	
}
