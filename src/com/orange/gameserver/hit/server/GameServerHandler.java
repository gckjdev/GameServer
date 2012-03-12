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

import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.service.AbstractRequestHandler;
import com.orange.gameserver.hit.service.GameService;
import com.orange.gameserver.hit.service.NewGameRequestHandler;
import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.GameRequest;
import com.orange.network.game.protocol.GameProtos.GameResponse;
import com.orange.network.game.protocol.GameProtos.NewGameRequest;



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
				
		GameProtos.GameRequest request = (GameProtos.GameRequest)e.getMessage();
		
		AbstractRequestHandler handler = null;
		if (request.getCommand() == GameProtos.GameRequest.CommandType.NEW_GAME){
			handler = new NewGameRequestHandler(e);
		}				
		
		if (handler == null){	
			sendErrorResponse(e, request, GameProtos.ResultCodeType.ERROR_SYSTEM_HANDLER_NOT_FOUND);
			return;
		}
		
		handler.handleRequest(request);			
	}
	
	public void sendErrorResponse(MessageEvent messageEvent, GameRequest request, GameProtos.ResultCodeType resultCode){
		GameProtos.GameResponse response = GameProtos.GameResponse.newBuilder().
			setId(request.getId()).
			setResultCode(resultCode).						
			build();

		logger.info(String.format("[%08X] [SEND] %s", response.getId(), response.toString()));
		messageEvent.getChannel().write(response);
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("GameServerHandler catch unexpected exception .", e.getCause());
		e.getChannel().close();
	}
			
//	private static String toString(Continent c) {
//		return "" + c.name().charAt(0) + c.name().toLowerCase().substring(1);
//	}
}
