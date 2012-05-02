package com.orange.gameserver.robot.client;

import java.util.Random;
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
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.gameserver.robot.RobotService;
import com.orange.gameserver.robot.client.RobotClient.ClientState;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.GeneralNotification;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameSession;

public class RobotClientHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = Logger
			.getLogger(RobotClientHandler.class.getName());
	ClientUser user;
	
	
	
	boolean startGameByMe = false;
	
	
	final RobotClient robotClient;

//	public RobotClientHandler() {
//		super();
//		// user = new ClientUser(ClientUser.getUid(), ClientUser.getUserName(),
//		// null);
//		user = ClientUser.getRandClinetUser();
//		ClientUserManager.addUser(user);
//	}

	public RobotClientHandler(RobotClient client) {
		this.robotClient = client;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		logger.info(e.toString());
		if (e instanceof ChannelStateEvent) {
		}
		super.handleUpstream(ctx, e);
	}

	int randValue(int number){
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
		return random.nextInt(number);
	}
	
	private void handleJoinGameResponse(GameMessage message){

		if (message.getResultCode() != GameResultCode.SUCCESS){
			GameLog.warn(robotClient.sessionId, "robot JOIN GAME failure, error="+message.getResultCode());
			robotClient.disconnect();
			return;
		}
		
//		PBGameSession session = message.getJoinGameResponse().getGameSession();
//		long sid = session.getSessionId();
//		String currentPlayer = session.getCurrentPlayUserId(); 
//		user.setSessionId(sid);
//		SessionManager.increaseCount(sid);
//		
//		logger.info("<DIDJOIN> " + user.getNickName() + " : "
//				+ user.getSessionId());
//		logger.info("<DIDJOIN>:" + SessionManager.getString());
//		
//		
//		
//		if (currentPlayer != null && currentPlayer.equalsIgnoreCase(user.getUserId())) {
//			startGame(randValue(5000));
//		}

	}
	
	
	private void handleQuitGameResponse(GameMessage message)
	{
		logger.info("<DIDQUIT> " + user.getNickName() + ": " + "quit from "
				+ user.getSessionId());
		SessionManager.decreaseCount(user.getSessionId());
		user.setSessionId(-1);
		logger.info("<DIDQUIT>" + SessionManager.getString());
//		service.sendJoinGameRequest(user);
	}
	
	private void handleStartGameResponse(GameMessage message) {
//		service.sendStartDraw(user, "杯子", 1);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		GameMessage message = (GameMessage) e.getMessage();
		
		switch (message.getCommand()){
		
		case JOIN_GAME_RESPONSE:
			handleJoinGameResponse(message);
			break;
						
		case START_GAME_RESPONSE:
			break;
			
		case NEW_DRAW_DATA_NOTIFICATION_REQUEST:
			handleDrawDataNotification(message);
			break;
			
		case GAME_TURN_COMPLETE_NOTIFICATION_REQUEST:			
			handleGameTurnCompleteNotification(message);
			break;
			
		case GAME_START_NOTIFICATION_REQUEST:
			handleGameStartNotification(message);
			break;

		case USER_JOIN_NOTIFICATION_REQUEST:			
			handleUserJoinNotification(message);
			break;
			
		case USER_QUIT_NOTIFICATION_REQUEST:
			handleUserQuitNotification(message);
			break;
		}

//		if (message.getCommand() == GameCommandType.JOIN_GAME_RESPONSE) {
//			handleJoinGameResponse(message);
//			
//		} else if (message.getCommand() == GameCommandType.QUIT_GAME_RESPONSE) {
//			handleQuitGameResponse(message);
//		}else if(message.getCommand() == GameCommandType.START_GAME_RESPONSE){
//			handleStartGameResponse(message);
//		}else if(message.getCommand() == GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST){
//			handleNewWordNotificationResponse(message);
//		}else if(message.getCommand() == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
//			handleGameCompleteNotificationResquest(message);
//		}else if(message.getCommand() == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
//			handleGameCompleteNotificationResquest(message);
//		}
	}

	private void handleGameTurnCompleteNotification(GameMessage message) {
		robotClient.setState(ClientState.WAITING);
		robotClient.updateByNotification(message.getNotification());
		
		robotClient.resetPlayData();
		
		// TODO check if the robot is the next user...
	}

	private void handleDrawDataNotification(GameMessage message) {
		robotClient.updateTurnData(message.getNotification());
		
		if (message.getNotification() == null)
			return;
			
		String word = message.getNotification().getWord();
		if (word != null && word.length() > 0){
			robotClient.setState(ClientState.PLAYING);
			robotClient.resetPlayData();
		}
		
		// now here need to simulate guess word...
		robotClient.setGuessWordTimer();
	}

	private void handleGameStartNotification(GameMessage message) {		
		robotClient.setState(ClientState.PICK_WORD);				
		robotClient.updateByNotification(message.getNotification());								
	}

	private void handleUserJoinNotification(GameMessage message) {
		
		robotClient.updateByNotification(message.getNotification());		
		
		if (robotClient.canQuitNow()){
			GameLog.info(robotClient.sessionId, "reach min user for session, robot can escape now!");
			robotClient.disconnect();
		}
	}

	private void handleUserQuitNotification(GameMessage message) {
		String userId = message.getNotification().getQuitUserId();
		if (userId == null){
			return;
		}
		
		robotClient.removeUserByUserId(userId);
		if (robotClient.sessionUserCount() == 0){
			// no other users, quit robot
			robotClient.sendQuitGameRequest();
			RobotService.getInstance().finishRobot(robotClient);
		}
	}

	Timer startTimer = null;

	private void startGame(long delay){
		
		if (startTimer != null){
			startTimer.cancel();
			startTimer = null;
		}
		
		startTimer = new Timer();
		startTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				logger.info("<Start> " + user.getNickName()+" start game, session in " + user.getSessionId());
//				service.sendStartRequst(user);
				startGameByMe = true;
//				logger.info("<StartCount>:"+ (++startCount));					
			}
		},delay);

	}
	
	private void handleGameCompleteNotificationResquest(GameMessage message) {
		String uid =  message.getNotification().getCurrentPlayUserId();
		
		// clear timer
		if (startTimer != null){
			startTimer.cancel();
			startTimer = null;
		}
		
		if (sendGuessWordTimer != null){
			sendGuessWordTimer.cancel();
			sendGuessWordTimer = null;
		}

		if (quitGameTimer != null){
			quitGameTimer.cancel();
			quitGameTimer = null;
		}
		
		startGameByMe = false;
		
		logger.info("<COMPLETE> reason="+message.getCompleteReason()+" next player " + uid);
		
		
		if (uid.equalsIgnoreCase(user.getUserId())) {
			startGame(5000);
		}
	}

	Timer sendGuessWordTimer = null;
	Timer quitGameTimer = null;
	
	private void handleNewWordNotificationResponse(GameMessage message) {
		
		final String word = message.getNotification().getWord();
		logger.info("<WORD>:"+ word);
//		if (word == null || word.length() == 0) {
//			return;
//		}
		
		
		int rand = this.randValue(5000);

//		boolean enableQuit = false;
		
		if (randValue(100) == 0 && !startGameByMe){
			// quit game randomly
			if (quitGameTimer != null){
				quitGameTimer.cancel();
				quitGameTimer = null;
			}

			quitGameTimer = new Timer();
			quitGameTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					logger.info("<QUIT> " + user.getNickName());
//					service.sendRunawayRequest(user);
				}
			},1000);	// quit after 1 second			
			
		}
		else{
			
			if (startGameByMe)		// don't send guess word due to game is started by me
				return;
			
			if (sendGuessWordTimer != null){
				sendGuessWordTimer.cancel();
				sendGuessWordTimer = null;
			}

			sendGuessWordTimer = new Timer();
			sendGuessWordTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					logger.info("<GUESS> " + user.getNickName() + " guess word ");
//					if ((Math.random() * 12121) % 3 == 1) {
//						service.sendGeussWordRequest(user, "杯子");	
//					}else{
//						service.sendGeussWordRequest(user, "屌丝");
//					}
//					service.sendGeussWordRequest(user, "杯子");
				}
			},rand);			
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		GameLog.info(robotClient.sessionId, "catch exception, cause="+e.getCause());
		e.getChannel().close();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) {
		GameLog.info(robotClient.sessionId, "<robotClient> channel disonnected");
		RobotService.getInstance().finishRobot(robotClient);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		GameLog.info(robotClient.sessionId, "<robotClient> channel connected");
		robotClient.setChannel(e.getChannel());		
		robotClient.sendJoinGameRequest();
	}
}
