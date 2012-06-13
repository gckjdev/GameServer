package com.orange.gameserver.draw.statemachine.game;

import org.apache.log4j.Logger;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.service.AbstractRequestHandler;
import com.orange.gameserver.draw.service.GameSessionRequestHandler;
import com.orange.gameserver.draw.service.HandlerUtils;
import com.orange.gameserver.draw.service.JoinGameRequestHandler;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;

public class CommonGameState extends State {

	protected static final Logger logger = Logger.getLogger("GameState");
	
	public CommonGameState(Object stateId) {
		super(stateId);
	}
	
	@Override
	public void enterAction(Event event, Object context) {
		
		GameEvent gameEvent = (GameEvent)event;
		GameSession session = (GameSession)context;		
		
		switch (gameEvent.getMessage().getCommand()){				
			case JOIN_GAME_REQUEST:
			case LOCAL_JOIN_GAME:
//				JoinGameRequestHandler.handleJoinGameRequest(gameEvent, session);
				break;
				
			case START_GAME_REQUEST:
				GameSessionRequestHandler.handleStartGameRequest(gameEvent, session);
				break;
				
			case CHAT_REQUEST:
				GameSessionRequestHandler.handleChatRequest(gameEvent, session);
				break;
				
			case QUIT_GAME_REQUEST:
				GameSessionRequestHandler.handleQuitGameRequest(gameEvent, session);
				break;

			case SEND_DRAW_DATA_REQUEST:
				GameSessionRequestHandler.handleSendDrawDataRequest(gameEvent, session);
				break;
			
			case CLEAN_DRAW_REQUEST:
				GameSessionRequestHandler.handleCleanDrawRequest(gameEvent, session);
				break;
				
			case LOCAL_CHANNEL_DISCONNECT:
				GameSessionRequestHandler.hanndleChannelDisconnect(gameEvent, session);
				break;
				
			case LOCAL_FINISH_GAME:
				GameSessionRequestHandler.hanndleFinishGame(gameEvent, session);
				break;
				
			case LOCAL_GAME_TURN_COMPLETE:
				GameSessionRequestHandler.handleTurnComplete(gameEvent, session);
				break;
				
			case LOCAL_USER_TIME_OUT:
				GameSessionRequestHandler.handleUserTimeOut(gameEvent, session);
				break;

			default:
				break;
		}				

		// handle event by sub class
		handleEvent(gameEvent, session);
	}
	
	@Override
	public int validateEvent(Event event, Object context) {
		
		GameEvent gameEvent = (GameEvent)event;
		GameSession session = (GameSession)context;		
		
		GameService gameService = GameService.getInstance();
		
		GameResultCode resultCode = GameResultCode.SUCCESS;

		switch (gameEvent.getMessage().getCommand()){				
		case JOIN_GAME_REQUEST:
			break;
			
		case START_GAME_REQUEST:
			resultCode = GameSessionRequestHandler.validateStartGameRequest(gameEvent, session);
			break;
			
		default:
			break;

		}
		
		if (resultCode != GameResultCode.SUCCESS){
			// send response directly here
			HandlerUtils.sendErrorResponse(gameEvent, resultCode);			
			return 1; // FAIL
		}
		
		return 0; // SUCC
	}
	
	public void handleEvent(GameEvent event, GameSession session){
	}
	
}
