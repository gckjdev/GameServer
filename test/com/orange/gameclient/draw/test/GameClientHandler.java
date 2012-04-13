package com.orange.gameclient.draw.test;


import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.orange.gameclient.draw.test.dao.ClientUser;
import com.orange.gameclient.draw.test.dao.ClientUserManager;
import com.orange.gameclient.draw.test.dao.SessionManager;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.sun.org.apache.bcel.internal.generic.InstructionConstants.Clinit;



public class GameClientHandler extends SimpleChannelUpstreamHandler {
	
	private static final Logger logger = Logger.getLogger(GameClientHandler.class
			.getName()); 
	ClientUser user;
	
	private ClientService service = ClientService.getInstanceClientService();
	
	public GameClientHandler() {
		super();
//		user = new ClientUser(ClientUser.getUid(), ClientUser.getUserName(), null);
		user = ClientUser.getRandClinetUser();
		ClientUserManager.addUser(user);
	}

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
			
		GameMessage message = (GameMessage) e.getMessage();
		if (message.getCommand() == GameCommandType.JOIN_GAME_RESPONSE) {
			
			long sid = message.getJoinGameResponse().getGameSession().getSessionId();
			user.setSessionId(sid);
			logger.info("<DIDJOIN> " + user.getNickName()+" : " + user.getSessionId());
			SessionManager.increaseCount(sid);
			logger.info(SessionManager.getString());
		}
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

		if (user != null && user.getSessionId() > 0) {
			long sid = user.getSessionId();
			logger.info("[QUIT]:"+user.getNickName() + "quit from session " + sid);
			user.setSessionId(-1); 
			SessionManager.decreaseCount(sid);			
		}	
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e){
		logger.info("GameClientHandler channel connected");
		user.setChannel(e.getChannel());
		logger.info("<JOIN> " + user.getNickName() + " start to join game");
		service.sendJoinGameRequest(user);
		
	}
}
