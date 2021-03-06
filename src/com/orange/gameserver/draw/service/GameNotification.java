package com.orange.gameserver.draw.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;

public class GameNotification {

//	protected static final Logger logger = Logger.getLogger(GameNotification.class.getName());
	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	private static final GameSessionManager sessionManager = GameSessionManager.getInstance();
	private static final GameService gameService = GameService.getInstance();
	
	
	public static void broadcastNotification(GameSession gameSession, String excludeUserId, GameCommandType command) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());	
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		for (User user : list){		
			if (excludeUserId != null && user.getUserId().equalsIgnoreCase(excludeUserId))
				continue;
			
			GameMessageProtos.GeneralNotification notification;
			
			if (command == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
				notification = GameMessageProtos.GeneralNotification.newBuilder()		
					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
					.setTurnGainCoins(gameSession.getCurrentUserGainCoins(user.getUserId()))
					.build();				
			}
			else{
				notification = GameMessageProtos.GeneralNotification.newBuilder()		
					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
					.build();
			}
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(command)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())
				.setCompleteReason(gameSession.getCompleteReason())
				.setNotification(notification)			
				.setRound(gameSession.getCurrentRound())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}
	}
	
	public static void broadcastNotification(GameSession gameSession,
			GameEvent gameEvent, String excludeUserId, GameCommandType command) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());	
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		for (User user : list){		
			if (excludeUserId != null && user.getUserId().equalsIgnoreCase(excludeUserId))
				continue;
			
			GameMessageProtos.GeneralNotification notification;
			
			if (command == GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST){
				notification = GameMessageProtos.GeneralNotification.newBuilder()		
					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
					.setTurnGainCoins(gameSession.getCurrentUserGainCoins(user.getUserId()))
					.build();				
			}
			else{
				notification = GameMessageProtos.GeneralNotification.newBuilder()		
					.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
					.build();
			}
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(command)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setCompleteReason(gameSession.getCompleteReason())
				.setNotification(notification)			
				.setRound(gameSession.getCurrentRound())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}

	public static void broadcastDrawUserChangeNotification(GameSession gameSession) {
		
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setNextPlayUserId("")
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(response, user.getChannel());
		}
	}
	
	public static void broadcastUserJoinNotification(GameSession gameSession,
			String newUserId, GameEvent gameEvent) {
		
		GameMessage request = gameEvent.getMessage();
		
		String newUserNickName = request.getJoinGameRequest().getNickName();
		String newUserAvatar = request.getJoinGameRequest().getAvatar();
		String newUserLocation = request.getJoinGameRequest().getLocation();
		int newUserLevel = request.getJoinGameRequest().getUserLevel();
		boolean newUserGender = false;
		if (request.getJoinGameRequest().hasGender()){
			newUserGender = request.getJoinGameRequest().getGender();
		}
		
		List<PBSNSUser> newUserSNSList = request.getJoinGameRequest().getSnsUsersList();
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){
			if (user.getUserId().equalsIgnoreCase(newUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setNewUserId(newUserId)
				.setNickName(newUserNickName)
				.setUserAvatar(newUserAvatar)
				.setUserGender(newUserGender)
				.setLocation(newUserLocation)
				.setUserLevel(newUserLevel)
				.addAllSnsUsers(newUserSNSList)
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setNextPlayUserId("")
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_JOIN_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, response, user.getChannel());
		}
	}
	
	public static void broadcastUserQuitNotification(GameSession gameSession, String quitUserId, 
			GameEvent gameEvent) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();

		for (User user : list){
			if (user.getUserId().equalsIgnoreCase(quitUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setQuitUserId(quitUserId)
				.setNextPlayUserId("")
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setSessionHost(gameSession.getHost())
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_QUIT_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, response, user.getChannel());
		}
	}
	
	public static void broadcastUserQuitNotification(GameSession gameSession, String quitUserId) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();

		for (User user : list){
			if (user.getUserId().equalsIgnoreCase(quitUserId)){
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setQuitUserId(quitUserId)
				.setNextPlayUserId("")
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setSessionHost(gameSession.getHost())
				.build();
			
			GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.USER_QUIT_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(response, user.getChannel());
		}
	}


	public static void broadcastGameStartNotification(GameSession gameSession, GameEvent gameEvent) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){			
			
			if (!user.isPlaying()){
				GameLog.info(gameSession.getSessionId(), "send START game notificaiton but user "+
						user.getNickName()+" not in play state");
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
				.setNextPlayUserId("")
				.build();
			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.GAME_START_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(user.getUserId())
				.setToUserId(user.getUserId())				
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
		
		boolean guessCorrect = false;
		int guessGainCoins = 0;
		if (drawData.hasGuessWord()){
			String currentWord = gameSession.getCurrentGuessWord();
			if (currentWord != null){
				guessCorrect = drawData.getGuessWord().equalsIgnoreCase(currentWord);
			}
			
			if (guessCorrect){
				guessGainCoins = gameSession.getCurrentGuessUserCoins(userId);
			}
		}
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){
			
			if (!user.isPlaying()){
				GameLog.info(gameSession.getSessionId(), "send DRAW REQUEST but user "+
						user.getNickName()+" not in play state");
				continue;
			}			
			
			if (!guessCorrect && user.getUserId().equalsIgnoreCase(userId))
				continue;
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.setColor(drawData.getColor())
				.addAllPoints(drawData.getPointsList())
				.setWidth(drawData.getWidth())
				.setPenType(drawData.getPenType())
				.setWord(drawData.getWord())
				.setLevel(drawData.getLevel())	
				.setLanguage(drawData.getLanguage())
				.setRound(gameSession.getCurrentRound())
				.setGuessWord(drawData.getGuessWord())
				.setGuessUserId(drawData.getGuessUserId())
				.setGuessCorrect(guessCorrect)
				.setGuessGainCoins(guessGainCoins)
				.build();
			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.NEW_DRAW_DATA_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setNotification(notification)
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}

	public static void broadcastCleanDrawNotification(GameSession gameSession,
			GameEvent gameEvent, String userId) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){	
			
			if (!user.isPlaying()){
				GameLog.info(gameSession.getSessionId(), "send CLEANDRAW but user "+
						user.getNickName()+" not in play state");
				continue;
			}	
			
			String toUserId = user.getUserId();
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())				
				.build();
			
			HandlerUtils.sendMessage(gameEvent, message, user.getChannel());
		}
	}
	
	public static void broadcastCleanDrawNotification(GameSession gameSession, String userId) {
		
		List<User> list = sessionUserManager.getUserListBySession(gameSession.getSessionId());
		for (User user : list){	
			
			if (!user.isPlaying()){
				GameLog.info(gameSession.getSessionId(), "send CLEANDRAW but user "+
						user.getNickName()+" not in play state");
				continue;
			}	
			
			String toUserId = user.getUserId();
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// send notification for the user			
			GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CLEAN_DRAW_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(gameSession.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())				
				.build();
			
			HandlerUtils.sendMessage(message, user.getChannel());
		}
	}

	public static void broadcastChatNotification(GameSession session,
			GameEvent gameEvent, String userId) {		
		
		GameMessage message = gameEvent.getMessage();
		if (message.getChatRequest() == null)
			return;
		
		GameChatRequest chatRequest = message.getChatRequest();
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();

		List<User> list = sessionUserManager.getUserListBySession(session.getSessionId());	
		List<String> toUserIdList = chatRequest.getToUserIdList();
		boolean hasTargetUser = (toUserIdList != null && toUserIdList.size() > 0);
		for (User user : list){		
			String toUserId = user.getUserId();
			
			// don't send to request user, he knows it!
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// if user is not in target user list, skip
			if (hasTargetUser && !toUserIdList.contains(toUserId)){				
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.addAllChatToUserId(toUserIdList)
				.setChatContent(chatRequest.getContent())
				.setChatType(chatRequest.getChatType())				
				.build();

			// send notification for the user			
			GameMessageProtos.GameMessage m = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CHAT_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setUserId(userId)
				.setToUserId(user.getUserId())				
				.setNotification(notification)
				.setRound(message.getRound())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(gameEvent, m, user.getChannel());
		}
	}

	public static void broadcastChatNotification(GameSession session,
			GameMessage message, String userId) {		
		
		if (message.getChatRequest() == null)
			return;
		
		GameChatRequest chatRequest = message.getChatRequest();
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();

		List<User> list = sessionUserManager.getUserListBySession(session.getSessionId());	
		List<String> toUserIdList = chatRequest.getToUserIdList();
		boolean hasTargetUser = (toUserIdList != null && toUserIdList.size() > 0);
		for (User user : list){		
			String toUserId = user.getUserId();
			
			// don't send to request user, he knows it!
			if (toUserId.equalsIgnoreCase(userId))
				continue;
			
			// if user is not in target user list, skip
			if (hasTargetUser && !toUserIdList.contains(toUserId)){				
				continue;
			}
			
			// send notification for the user
			GameMessageProtos.GeneralNotification notification = GameMessageProtos.GeneralNotification.newBuilder()		
				.addAllChatToUserId(toUserIdList)
				.setChatContent(chatRequest.getContent())
				.setChatType(chatRequest.getChatType())				
				.build();

			// send notification for the user			
			GameMessageProtos.GameMessage m = GameMessageProtos.GameMessage.newBuilder()
				.setCommand(GameCommandType.CHAT_NOTIFICATION_REQUEST)
				.setMessageId(GameService.getInstance().generateMessageId())
				.setSessionId(session.getSessionId())
				.setUserId(userId)
				.setToUserId(toUserId)				
				.setNotification(notification)
				.setRound(message.getRound())
				.setOnlineUserCount(onlineUserCount)
				.build();
			
			HandlerUtils.sendMessage(m, user.getChannel());
		}
	}	
}
