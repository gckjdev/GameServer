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
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;

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
		
		JoinGameRequest joinRequest = request.getJoinGameRequest();
		
		String userId = joinRequest.getUserId();
		String gameId = joinRequest.getGameId();
		String nickName = joinRequest.getNickName();			
		String avatar = joinRequest.getAvatar();
		boolean gender = joinRequest.getGender();
		String location = joinRequest.getLocation();
		List<PBSNSUser> snsUser = joinRequest.getSnsUsersList();

		int guessDifficultLevel = 1;
		if (joinRequest.hasGuessDifficultLevel())
			guessDifficultLevel = joinRequest.getGuessDifficultLevel(); 		
		
		int gameSessionId = -1;
		
		if (joinRequest.hasRoomId()){
			String roomId = joinRequest.getRoomId();
			String roomName = joinRequest.getRoomName();
			GameSession session = gameManager.allocFriendRoom(roomId, roomName, userId, nickName, avatar, gender,
					location, snsUser,
					guessDifficultLevel, messageEvent.getChannel());
			if (session == null){
				HandlerUtils.sendErrorResponse(request, GameResultCode.ERROR_NO_SESSION_AVAILABLE, messageEvent.getChannel());
				return;
			}
			else{
				gameSessionId = session.getSessionId();
			}						
		}
		else if (joinRequest.hasTargetSessionId()){
			gameSessionId = joinRequest.getTargetSessionId();
			boolean isRobot = false;
			if (joinRequest.hasIsRobot()){
				isRobot = joinRequest.getIsRobot();
			}
			
			GameResultCode result = gameManager.directPutUserIntoSession(userId, nickName, avatar, gender, 
					location, snsUser,
					guessDifficultLevel,
					messageEvent.getChannel(), isRobot, gameSessionId);
			if (result != GameResultCode.SUCCESS){
				HandlerUtils.sendErrorResponse(request, result, messageEvent.getChannel());
				return;
			}					
		}
		else{		
			gameSessionId = gameManager.allocGameSessionForUser(userId, nickName, avatar, gender,
					location, snsUser,
					guessDifficultLevel, messageEvent.getChannel(), null);
			if (gameSessionId == -1){
				HandlerUtils.sendErrorResponse(request, GameResultCode.ERROR_NO_SESSION_AVAILABLE, messageEvent.getChannel());
				return;
			}
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
		boolean gender = false;
		if (request.getJoinGameRequest().hasGender()){
			gender = request.getJoinGameRequest().getGender();
		}
		
		String location = request.getJoinGameRequest().getLocation();
		List<PBSNSUser> snsUser = request.getJoinGameRequest().getSnsUsersList();
		
		int guessDifficultLevel = 1;
		if (request.getJoinGameRequest().hasGuessDifficultLevel())
			guessDifficultLevel = request.getJoinGameRequest().getGuessDifficultLevel(); 		
		
		if (request.getJoinGameRequest().hasSessionToBeChange()){
			GameSessionRequestHandler.handleChangeRoomRequest(gameEvent, gameSession);
			return true;
		}
		
		// add user
		int sessionId = gameSession.getSessionId();
//		User user = new User(userId, nickName, avatar, gender, gameEvent.getChannel(), sessionId, guessDifficultLevel);
//		sessionUserManager.addUserIntoSession(user, gameSession);
		sessionManager.addUserIntoSession(userId, nickName, avatar, gender,
				location, snsUser,
				guessDifficultLevel, 
				request.getJoinGameRequest().getIsRobot(), gameEvent.getChannel(), gameSession);
		int onlineUserCount = UserManager.getInstance().getOnlineUserCount();
		
		// reset start expire timer for current play user
		gameSession.startStartExpireTimerIfNeeded();
		
		// schedule robot timer if needed
		GameSessionManager.getInstance().prepareRobotTimer(gameSession);
		
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
					.setOnlineUserCount(onlineUserCount)
					.setJoinGameResponse(joinGameResponse)
					.build();

		HandlerUtils.sendResponse(gameEvent, response);
		
		// send notification to all other users in the session
		GameNotification.broadcastUserJoinNotification(gameSession, userId, gameEvent);
		
		return true;
	}

	

	

}
