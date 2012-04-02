package com.orange.gameserver.draw.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.channel.MessageEvent;

import sun.tools.tree.ThisExpression;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameChatRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;

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
		
//		if (!session.canUserStartGame(userId))
//			return GameResultCode.ERROR_USER_CANNOT_START_GAME;		
		
		return GameResultCode.SUCCESS;
	}

	public static void handleStartGameRequest(GameEvent gameEvent,
			GameSession session) {
		
		// set current play user id and next play user id
		// session.chooseNewPlayUser();				
		
		// start game
		session.startGame();
		GameSessionManager.getInstance().adjustSessionSetForPlaying(session); // adjust set so that it's not allowed to join
				
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
		
		GameMessage message = gameEvent.getMessage();
		SendDrawDataRequest drawRequest = message.getSendDrawDataRequest();
		if (drawRequest == null){
			return;
		}

		if (drawRequest.hasWord()){
			session.startNewTurn(drawRequest.getWord(), drawRequest.getLevel());

			// schedule timer for finishing this turn
			GameService.getInstance().scheduleGameSessionExpireTimer(session);
		}
		
		if (drawRequest.getPointsCount() > 0){
			// TODO save draw data into turn data
		}
		
		if (drawRequest.hasGuessWord()){
			session.userGuessWord(drawRequest.getGuessUserId(), drawRequest.getGuessWord());
		}
				
		// broast draw data to all other users in the session
		GameNotification.broadcastDrawDataNotification(session, gameEvent, gameEvent.getMessage().getUserId());
		
		if (sessionManager.isSessionTurnFinish(session)){
			GameService.getInstance().fireTurnFinishEvent(session);
		}
	}

	public static void handleCleanDrawRequest(GameEvent gameEvent,
			GameSession session) {
		
		// TODO save clean draw into turn data 		
		
		// broast draw data to all other users in the session
		GameNotification.broadcastCleanDrawNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}
	
	public static void handleChatRequest(GameEvent gameEvent,
			GameSession session) {
		
		GameChatRequest chatRequest = gameEvent.getMessage().getChatRequest();
		if (chatRequest == null)
			return;				
		
		// TODO record chat data into turn
		
		// broast draw data to all other users in the session
		GameNotification.broadcastChatNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}

	public static void userQuitSession(GameEvent gameEvent,
			GameSession session) {
		
		GameMessage message = gameEvent.getMessage();

		// if draw user quit, then game also completed
		boolean completeGameTurn = false;
		String userId = gameEvent.getMessage().getUserId();
		if (session.isCurrentPlayUser(userId)){
			completeGameTurn = true;
		}
		
		int sessionId = session.getSessionId();

		GameSessionManager.getInstance().removeUserFromSession(message.getUserId(), session);
		if (sessionUserManager.getSessionUserCount(sessionId) <= 1){
			completeGameTurn = true;			
		}
		
		boolean completeGame = false;
		if (sessionUserManager.getSessionUserCount(sessionId) == 0){
			completeGame = true;
		}
		
		if (completeGame){
			// if there is no user, fire a finish message
			GameService.getInstance().fireAndDispatchEventHead(GameCommandType.LOCAL_FINISH_GAME, 
					session.getSessionId(), null);
		}
		else{
			// broadcast user exit message to all other users
			GameNotification.broadcastUserQuitNotification(session, message.getUserId(), gameEvent);			
		}	
		
		if (completeGameTurn){
			GameService.getInstance().fireTurnFinishEvent(session);
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

		User user = userManager.findUserById(gameEvent.getMessage().getUserId());
		if (user == null){
			logger.info("<handleChangeRoomRequest> but user id "+gameEvent.getMessage().getUserId()
					+" not found at session "+session.getSessionId());
			HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.ERROR_USER_NOT_IN_SESSION);
			return;
		}

		String nickName = user.getNickName();
		String avatar = user.getAvatar();

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
				nickName, avatar, gameEvent.getChannel(), excludeSessionSet);
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
			
			GameService.getInstance().dispatchEvent(event);				
		}
		else{
			// no session available, send back error response
			HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.ERROR_NO_SESSION_AVAILABLE);
		}
	}

	public static void handleTurnComplete(GameEvent gameEvent,
			GameSession session) {
		
		// turn complete, select next user and start new turn
		sessionUserManager.chooseNewPlayUser(session);
//		session.chooseNewPlayUser();
		
		GameNotification.broadcastNotification(session, gameEvent, "", 
				GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST);
	}

	public static void handleQuitGameRequest(GameEvent gameEvent,
			GameSession session) {
		
		userQuitSession(gameEvent, session);	
		
		// send back response
		HandlerUtils.sendErrorResponse(gameEvent, GameResultCode.SUCCESS);
	}
}
