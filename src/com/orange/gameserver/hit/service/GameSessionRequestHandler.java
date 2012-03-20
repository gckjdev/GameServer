package com.orange.gameserver.hit.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.channel.MessageEvent;

import sun.tools.tree.ThisExpression;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.dao.UserAtGame;
import com.orange.gameserver.hit.manager.GameManager;
import com.orange.gameserver.hit.manager.UserManager;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;

public class GameSessionRequestHandler extends AbstractRequestHandler {

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
		
		if (!session.canUserStartGame(userId))
			return GameResultCode.ERROR_USER_CANNOT_START_GAME;		
		
		return GameResultCode.SUCCESS;
	}

	public static void handleStartGameRequest(GameEvent gameEvent,
			GameSession session) {
		
		// set current play user id and next play user id
		// session.chooseNewPlayUser();				
		
		// start game
		session.startGame();
		GameManager.getInstance().adjustSessionSetForPlaying(session); // adjust set so that it's not allowed to join
				
		// send reponse
		GameMessageProtos.StartGameResponse gameResponse = GameMessageProtos.StartGameResponse.newBuilder()
			.setCurrentPlayUserId(session.getCurrentPlayUserId())
			.setNextPlayUserId(session.getNextPlayUserId())
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
		
		if (session.isTurnFinish()){
			GameService.getInstance().fireTurnFinishEvent(session);
		}
	}

	public static void handleCleanDrawRequest(GameEvent gameEvent,
			GameSession session) {
		
		// TODO save clean draw into turn data 		
		
		// broast draw data to all other users in the session
		GameNotification.broadcastCleanDrawNotification(session, gameEvent, gameEvent.getMessage().getUserId());
	}
	
	public static void handleProlongGameRequest(GameEvent gameEvent,
			GameSession session) {
		// broast draw data to all other users in the session
		GameNotification.broadcastProlongGameNotification(session, gameEvent, gameEvent.getMessage().getUserId());
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

		GameManager.getInstance().removeUserFromSession(message.getUserId(), session);
		boolean completeGame = false;
		if (session.isRoomEmpty()){
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

		UserAtGame user = session.findUserById(gameEvent.getMessage().getUserId());
		if (user == null){
			logger.info("<handleChangeRoomRequest> but user id "+gameEvent.getMessage().getUserId()
					+" not found at session "+session.getSessionId());
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
		int sessionId = GameManager.getInstance().allocGameSessionForUser(message.getUserId(), 
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
