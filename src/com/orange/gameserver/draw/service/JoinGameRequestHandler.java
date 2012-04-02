package com.orange.gameserver.draw.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.statemachine.game.GameEventKey;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.model.GameBasicProtos;

public class JoinGameRequestHandler extends AbstractRequestHandler {

	private static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	private static final GameSessionManager sessionManager = GameSessionManager.getInstance();
	private static final GameService gameService = GameService.getInstance();
	private static final UserManager userManager = UserManager.getInstance();
	
	public JoinGameRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage request) {
		
		String userId = request.getJoinGameRequest().getUserId();
		String gameId = request.getJoinGameRequest().getGameId();
		String nickName = request.getJoinGameRequest().getNickName();			
		String avatar = request.getJoinGameRequest().getAvatar();
		
		int gameSessionId = gameManager.allocGameSessionForUser(userId, nickName, avatar, messageEvent.getChannel(), null);
		if (gameSessionId == -1){
			HandlerUtils.sendErrorResponse(request, GameResultCode.ERROR_NO_SESSION_AVAILABLE, messageEvent.getChannel());
			return;
		}
				
		GameEvent gameEvent = new GameEvent(
				GameCommandType.JOIN_GAME_REQUEST, 
				gameSessionId, 
				request, 
				messageEvent.getChannel());
		
		gameService.dispatchEvent(gameEvent);				
	}

	public static boolean handleJoinGameRequest(GameEvent gameEvent,
			GameSession gameSession) {

		GameMessage request = gameEvent.getMessage();
				
		String userId = request.getJoinGameRequest().getUserId();
		String nickName = request.getJoinGameRequest().getNickName();
		String avatar = request.getJoinGameRequest().getAvatar();
		
		if (request.getJoinGameRequest().hasSessionToBeChange()){
			GameSessionRequestHandler.handleChangeRoomRequest(gameEvent, gameSession);
			return true;
		}
		
		// add user
		int sessionId = gameSession.getSessionId();
		User user = new User(userId, nickName, avatar, gameEvent.getChannel(), sessionId);
		sessionUserManager.addUserIntoSession(user, gameSession);
		
		// send back response
		List<GameBasicProtos.PBGameUser> pbGameUserList = sessionUserManager.usersToPBUsers(sessionId);	
		GameBasicProtos.PBGameSession gameSessionData = GameBasicProtos.PBGameSession.newBuilder()		
										.setGameId("DrawGame")
										.setCurrentPlayUserId(gameSession.getCurrentPlayUserId())
										.setNextPlayUserId("")
										.setHost(gameSession.getHost())
										.setName(gameSession.getName())
										.setSessionId(gameSession.getSessionId())
										.addAllUsers(pbGameUserList)
										.build();

		GameMessageProtos.JoinGameResponse joinGameResponse = GameMessageProtos.JoinGameResponse.newBuilder()
										.setGameSession(gameSessionData)
										.build();
		
		GameMessageProtos.GameMessage response = GameMessageProtos.GameMessage.newBuilder()
					.setCommand(GameCommandType.JOIN_GAME_RESPONSE)
					.setMessageId(request.getMessageId())
					.setResultCode(GameResultCode.SUCCESS)
					.setJoinGameResponse(joinGameResponse)
					.build();

		HandlerUtils.sendResponse(gameEvent, response);
		
		// send notification to all other users in the session
		GameNotification.broadcastUserJoinNotification(gameSession, userId, gameEvent);
		
		return true;
	}

	

	

}
