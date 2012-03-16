package com.orange.gameserver.hit.statemachine.game;

import org.apache.log4j.Logger;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.server.GameService;
import com.orange.gameserver.hit.service.AbstractRequestHandler;
import com.orange.gameserver.hit.service.GameSessionRequestHandler;
import com.orange.gameserver.hit.service.HandlerUtils;
import com.orange.gameserver.hit.service.JoinGameRequestHandler;
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
				JoinGameRequestHandler.handleJoinGameRequest(gameEvent, session);
				break;
				
			case START_GAME_REQUEST:
				GameSessionRequestHandler.handleStartGameRequest(gameEvent, session);
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
