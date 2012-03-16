package com.orange.gameserver.hit.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.UserAtGame;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;
import com.orange.network.game.protocol.model.GameBasicProtos;

public class GameNotification {

	protected static final Logger logger = Logger.getLogger(GameNotification.class.getName());
	
	public static void broadcastUserJoinNotification(GameSession gameSession,
			String newUserId, GameEvent gameEvent) {
		
		List<UserAtGame> list = gameSession.getUserList();
		for (UserAtGame user : list){
			if (user.getUserId().equals(newUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setNewUserId(newUserId)
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.build();
			
			HandlerUtils.sendMessage(gameEvent, response, user.getChannel());
		}
	}
	
	public static void broadcastUserQuitNotification(GameSession gameSession, String quitUserId,
			GameEvent gameEvent) {
		
		List<UserAtGame> list = gameSession.getUserList();
		for (UserAtGame user : list){
			if (user.getUserId().equals(quitUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setQuitUserId(quitUserId)
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_QUIT_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.build();
			
			HandlerUtils.sendMessage(gameEvent, response, user.getChannel());
		}
	}


	public static void broadcastGameStartNotification(GameSession gameSession, GameEvent gameEvent) {
		
		List<UserAtGame> list = gameSession.getUserList();
		for (UserAtGame user : list){			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setNextPlayUserId(gameSession.getNextPlayUserId())
				.build();
			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.GAME_START_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}

	public static void broadcastDrawDataNotification(GameSession gameSession,
			GameEvent gameEvent, String userId) {
		
		SendDrawDataRequest drawData = gameEvent.getMessage().getSendDrawDataRequest();
		if (drawData == null){
			return;
		}
		
		List<UserAtGame> list = gameSession.getUserList();
		for (UserAtGame user : list){		
			if (user.getUserId().equals(userId))
				continue;
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setColor(drawData.getColor())
				.addAllPoints(drawData.getPointsList())
				.setWidth(drawData.getWidth())
				.build();
			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}

	public static void broadcastCleanDrawNotification(GameSession gameSession,
			GameEvent gameEvent, String userId) {
		
		List<UserAtGame> list = gameSession.getUserList();
		for (UserAtGame user : list){		
			if (user.getUserId().equals(userId))
				continue;
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}

}
