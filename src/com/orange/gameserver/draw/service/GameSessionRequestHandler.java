package com.orange.gameserver.draw.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.manager.ChannelUserManager;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.RoomSessionManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;

public class GameSessionRequestHandler extends AbstractRequestHandler {

	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	private static final GameSessionManager sessionManager = GameSessionManager.getInstance();
	private static final GameService gameService = GameService.getInstance();
	private static final UserManager userManager = UserManager.getInstance();
	
	public GameSessionRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message) {
		gameService.dispatchEvent(this.toGameEvent(gameMessage));
	}


	public static GameResultCode validateStartGameRequest(GameEvent gameEvent, GameSession session) {
		String userId = gameEvent.getMessage().getUserId();
		if (userId == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (session == null)
			return GameResultCode.ERROR_SESSIONID_NULL;
		
		if (session.isStart())
			return GameResultCode.ERROR_SESSION_ALREADY_START;
		
		return GameResultCode.SUCCESS;
	}

	public static void handleStartGameRequest(GameEvent gameEvent,
			GameSession session) {
					
		// start game
		session.startGame();
		session.clearStartExpireTimer();
		GameSessionManager.getInstance().adjustSessionSetForPlaying(session); // adjust set so that it's not allowed to join
		sessionUserManager.setUserPlaying(session);
		
		// send reponse
		GameMessageProtos.StartGameResponse gameResponse = GameMessageProtos.StartGameResponse.newBuilder()
			.setCurrentPlayUserId(session.getCurrentPlayUserId())
			.setNextPlayUserId("")
			.build();
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.START_GAME_RESPONSE)
			.setMessageId(gameEvent.getMessage().getMessageId())
			.setResultCode(GameResultCode.SUCCESS)
			.setStartGameResponse(gameResponse)
			.build();
		HandlerUtils.sendResponse(gameEvent, response);
		
		// broast to all users in the session
		GameNotification.broadcastGameStartNotification(session, gameEvent);
	}
	
	

	
	public static GameResultCode validateSendDrawDataRequest(GameEvent gameEvent, GameSession session) {
		String userId = gameEvent.getMessage().getUserId();
		if (userId == null)
			return GameResultCode.ERROR_USERID_NULL;
		
		if (session == null)
			return GameResultCode.ERROR_SESSIONID_NULL;
		
		if (!session.isStart())
			return GameResultCode.ERROR_SESSION_NOT_START;
		
		if (!gameEvent.getMessage().hasSendDrawDataRequest())
			return GameResultCode.ERROR_NO_DRAW_DATA;
		
		return GameResultCode.SUCCESS;
	}

	public static void handleSendDrawDataRequest(GameEvent gameEvent,
			GameSession session) {
		
		GameCompleteReason reason = GameCompleteReason.REASON_NOT_COMPLETE;
		
		GameMessage message = gameEvent.getMessage();
		SendDrawDataRequest drawRequest = message.getSendDrawDataRequest();
		if (drawRequest == null){
			return;
		}

		if (drawRequest.hasWord()){
			session.startNewTurn(drawRequest.getWord(), drawRequest.getLevel(), drawRequest.getLanguage());

			// schedule timer for finishing this turn
//			session.clearStartExpireTimer();
			gameService.scheduleGameSessionExpireTimer(session);
		}
		
		if (drawRequest.getPointsCount() > 0){
			// TODO save draw data into turn data
			session.appendDrawData(drawRequest.getPointsList(),
					drawRequest.getColor(),
					drawRequest.getWidth());
		}
		
		if (drawRequest.hasGuessWord()){
//			GameLog.info(session.getSessionId(), "user "+drawRequest.getGuessUserId()+ 
//					" guess "+drawRequest.getGuessWord());
			User guessUser = userManager.findUserById(drawRequest.getGuessUserId());
			session.userGuessWord(guessUser, drawRequest.getGuessWord());
		}				
				
		// broast draw data to all other users in the session
		GameNotification.broadcastDrawDataNotification(session, gameEvent, gameEvent.getMessage().getUserId());
		
		reason = sessionManager.isSessionTurnFinish(session);
		if (reason != GameCompleteReason.REASON_NOT_COMPLETE){
			gameService.fireTurnFinishEvent(session, reason);
		}
	}

	public static void handleCleanDrawRequest(GameEvent gameEvent,
			GameSession session) {
		
		// TODO save clean draw into turn data 		
		session.appendCleanDrawAction();
		
		// broast draw data to all other users in the session
		GameNotification.broadcastCleanDrawNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}
	
	public static void handleChatRequest(GameEvent gameEvent,
			GameSession session) {
		
		GameChatRequest chatRequest = gameEvent.getMessage().getChatRequest();
		if (chatRequest == null)
			return;				
		
		String fromUserId = gameEvent.getMessage().getUserId();
		if (session.getCurrentPlayUserId().equals(fromUserId)){
			session.resetStartExpireTimer();
		}
		
		// TODO record chat data into turn
		
		
		// broast draw data to all other users in the session
		GameNotification.broadcastChatNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}

	public static void userQuitSession(GameEvent gameEvent,
			GameSession session) {
		
		
		GameCompleteReason reason = GameCompleteReason.REASON_NOT_COMPLETE;
		GameMessage message = gameEvent.getMessage();

		// if draw user quit, then game also completed
		boolean completeGameTurn = false;
		String userId = gameEvent.getMessage().getUserId();

		int sessionId = session.getSessionId();
		GameLog.info(sessionId, "user "+userId+" quit");
		
		if (session.isCurrentPlayUser(userId)){
			sessionManager.adjustCurrentPlayerForUserQuit(session, userId);
			completeGameTurn = true;
			reason = GameCompleteReason.REASON_DRAW_USER_QUIT;
		}

		GameSessionManager.getInstance().removeUserFromSession(userId, session);
		if (!completeGameTurn){
			int sessionUserCount = sessionUserManager.getSessionUserCount(sessionId);
			if (sessionUserCount <= 1){
				reason = GameCompleteReason.REASON_ONLY_ONE_USER;
				completeGameTurn = true;			
			}
			else if (sessionManager.isAllUserGuessWord(session)){
				reason = GameCompleteReason.REASON_ALL_USER_GUESS;
				completeGameTurn = true;			
			}
		}
		
		boolean completeGame = false;
		int userCount = sessionUserManager.getSessionUserCount(sessionId);
		if (userCount == 0){
			completeGame = true;
		}
		
		if (completeGameTurn){
			gameService.fireTurnFinishEvent(session, reason);
		}		

		if (completeGame){
			// if there is no user, fire a finish message
			gameService.fireAndDispatchEvent(GameCommandType.LOCAL_FINISH_GAME, 
					session.getSessionId(), null);
		}
		else{
			// broadcast user exit message to all other users
			GameNotification.broadcastUserQuitNotification(session, message.getUserId(), gameEvent);			
		}	

		if (!completeGame && userCount == 1){
			sessionManager.prepareRobotTimer(session);
		}
		
		if (!completeGameTurn){
			session.startStartExpireTimerIfNeeded();
		}
		
	}
	
	public static void hanndleChannelDisconnect(GameEvent gameEvent,
			GameSession session) {
				
		userQuitSession(gameEvent, session);		
	}

	public static void hanndleFinishGame(GameEvent gameEvent,
			GameSession session) {
		session.resetGame();
	}

	public static void handleChangeRoomRequest(GameEvent gameEvent,
			GameSession session) {
		
		JoinGameRequest request = gameEvent.getMessage().getJoinGameRequest();
		if (request == null){
			GameLog.info(session.getSessionId(), "<handleChangeRoomRequest> but request null ");
			HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.ERROR_JOIN_GAME);
			return;			
		}

		String nickName = request.getNickName();
		String avatar = request.getAvatar();
		String location = request.getLocation();
		List<PBSNSUser> snsUser = request.getSnsUsersList();
		
		boolean gender = request.getGender();
		int guessDifficultLevel = 1;
		if (request.hasGuessDifficultLevel())
			guessDifficultLevel = request.getGuessDifficultLevel(); 		

		// user quit session
		userQuitSession(gameEvent, session);
				
		GameMessage message = gameEvent.getMessage();		
		
		// create exclude session set
		Set<Integer> excludeSessionSet = new HashSet<Integer>();
		List<Long> list = message.getJoinGameRequest().getExcludeSessionIdList();
		if (list != null){
			for (Long i : list){
				excludeSessionSet.add(i.intValue());
			}
		}
		
		// alloc user to new room
		int sessionId = GameSessionManager.getInstance().allocGameSessionForUser(message.getUserId(), 
				nickName, avatar, gender,
				location, snsUser,
				guessDifficultLevel, gameEvent.getChannel(), excludeSessionSet);
		if (sessionId != -1){
						
			JoinGameRequest joinRequest = GameMessageProtos.JoinGameRequest.newBuilder(message.getJoinGameRequest())
					.clearSessionToBeChange()
					.build();
			
			GameMessage newMessage = GameMessageProtos.GameMessage.newBuilder(message)
					.setJoinGameRequest(joinRequest)
					.build();

			GameEvent event = new GameEvent(
					GameCommandType.JOIN_GAME_REQUEST, 
					sessionId, 
					newMessage, 
					gameEvent.getChannel());
			
			gameService.dispatchEvent(event);				
		}
		else{
			// no session available, send back error response
			HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.ERROR_NO_SESSION_AVAILABLE);
		}
	}

	public static void handleTurnComplete(GameEvent gameEvent,
			GameSession session) {
		
		// turn complete, select next user and start new turn
		session.completeTurn(gameEvent.getMessage().getCompleteReason());
		session.resetExpireTimer();
		session.calculateDrawUserCoins();
		sessionUserManager.chooseNewPlayUser(session);		
		sessionManager.adjustSessionSetForTurnComplete(session);
//		session.chooseNewPlayUser();
		
		sessionUserManager.clearUserPlaying(session);
		
		GameNotification.broadcastNotification(session, gameEvent, "", 
				GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST);
	}

	public static void handleQuitGameRequest(GameEvent gameEvent,
			GameSession session) {
		
		userQuitSession(gameEvent, session);	
		
		// send back response
		HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.SUCCESS);
	}

	public static void handleUserTimeOut(GameEvent gameEvent,
			GameSession session) {

		if (sessionUserManager.getSessionUserCount(session.getSessionId()) == 1){
			GameLog.info(session.getSessionId(), "<UserTimeOut> but only has one user, userId="+
					gameEvent.getMessage().getUserId());
			session.resetStartExpireTimer();
			return;
		}

		// quit user
		userQuitSession(gameEvent, session);		
		
		if (gameEvent.getChannel() != null){
			ChannelUserManager.getInstance().processDisconnectChannel(gameEvent.getChannel());
		}
	}
}
