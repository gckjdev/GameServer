package com.orange.gameclient.draw.test;

import java.util.Timer;
import java.util.TimerTask;

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
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;

public class GameClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger
			.getLogger(GameClientHandler.class.getName());
	ClientUser user;

	private static int quitCount = 0;
	private static int joinCount = 0;
	private static int startCount = 0;
	private ClientService service = ClientService.getInstanceClientService();

	public GameClientHandler() {
		super();
		// user = new ClientUser(ClientUser.getUid(), ClientUser.getUserName(),
		// null);
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

	
	private void handleJoinGameResponse(GameMessage message){

		PBGameSession session = message.getJoinGameResponse().getGameSession();
		long sid = session.getSessionId();
		String currentPlayer = session.getCurrentPlayUserId(); 
		user.setSessionId(sid);
		SessionManager.increaseCount(sid);
		
		logger.info("<DIDJOIN> " + user.getNickName() + " : "
				+ user.getSessionId());
		logger.info("<DIDJOIN>:" + SessionManager.getString());
		logger.info("<JOINCOUNT>:" + joinCount++);
		
		if (currentPlayer != null && currentPlayer.equalsIgnoreCase(user.getUserId())) {
			startGame(10000);
		}

	}
	
	
	private void handleQuitGameResponse(GameMessage message)
	{
		logger.info("<DIDQUIT> " + user.getNickName() + ": " + "quit from "
				+ user.getSessionId());
		SessionManager.decreaseCount(user.getSessionId());
		user.setSessionId(-1);
		logger.info("<DIDQUIT>" + SessionManager.getString());
		logger.info("<QUITCOUNT>:" + quitCount++);
		service.sendJoinGameRequest(user);
	}
	
	private void handleStartGameResponse(GameMessage message) {
		service.sendStartDraw(user, "杯子", 1);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		GameMessage message = (GameMessage) e.getMessage();

		if (message.getCommand() == GameCommandType.JOIN_GAME_RESPONSE) {
			handleJoinGameResponse(message);
			
		} else if (message.getCommand() == GameCommandType.QUIT_GAME_RESPONSE) {
			handleQuitGameResponse(message);
		}else if(message.getCommand() == GameCommandType.START_GAME_RESPONSE){
			handleStartGameResponse(message);
		}else if(message.getCommand() == GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST){
			handleNewWordNotificationResponse(message);
		}else if(message.getCommand() == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
			handleGameCompleteNotificationResquest(message);
		}
	}



	private void startGame(long delay){
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				logger.info("<Start> " + user.getNickName()+" start game, session in " + user.getSessionId());
				service.sendStartRequst(user);
				logger.info("<StartCount>:"+ (++startCount));					
			}
		},delay);

	}
	
	private void handleGameCompleteNotificationResquest(GameMessage message) {
		String uid =  message.getNotification().getCurrentPlayUserId();
		
		logger.info("<COMPLETE>"+" next player " + uid);
		
		if (uid.equalsIgnoreCase(user.getUserId())) {
			startGame(5000);
		}
	}

	private void handleNewWordNotificationResponse(GameMessage message) {
		
		final String word = message.getSendDrawDataRequest().getGuessWord(); 
		logger.info("<WORD>:"+ word);
//		if (word == null || word.length() == 0) {
//			return;
//		}
		
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				logger.info("<GUESS> " + user.getNickName() + " guess word ");
//				if ((Math.random() * 12121) % 3 == 1) {
//					service.sendGeussWordRequest(user, "杯子");	
//				}else{
//					service.sendGeussWordRequest(user, "屌丝");
//				}
				service.sendGeussWordRequest(user, "杯子");
			}
		},5000);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("GameServerHandler catch unexpected exception .", e
				.getCause());
		e.getChannel().close();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) {
		logger.info("GameServerHandler channel disconnected");

		if (user != null && user.getSessionId() > 0) {
			long sid = user.getSessionId();
			logger.info("[QUIT]:" + user.getNickName() + "quit from session "
					+ sid);
			user.setSessionId(-1);
			SessionManager.decreaseCount(sid);
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.info("GameClientHandler channel connected");
		user.setChannel(e.getChannel());
		logger.info("<JOIN> " + user.getNickName() + " start to join game");
		service.sendJoinGameRequest(user);

	}
}
