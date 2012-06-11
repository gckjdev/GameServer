package com.orange.gameserver.draw.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;

public class DrawDataRequestHandler extends AbstractRequestHandler {

	public DrawDataRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	public DrawDataRequestHandler(GameEvent event) {
		super(event);		
	}

	@Override
	public void handleRequest(GameMessage message) {
		
		GameEvent gameEvent = toGameEvent(message);
		GameSession session = sessionManager.findGameSessionById((int) message.getSessionId());
		GameCommandType stateMachineCommandType = null;
		
		GameCompleteReason reason = GameCompleteReason.REASON_NOT_COMPLETE;
		
		SendDrawDataRequest drawRequest = message.getSendDrawDataRequest();
		if (drawRequest == null){
			return;
		}

		if (drawRequest.getPointsCount() > 0){
			session.appendDrawData(drawRequest.getPointsList(),
					drawRequest.getColor(),
					drawRequest.getWidth());
		}
				
		if (drawRequest.hasWord()){
			session.startNewTurn(drawRequest.getWord(), drawRequest.getLevel(), drawRequest.getLanguage());

			// schedule timer for finishing this turn
//			gameService.scheduleGameSessionExpireTimer(session);
			
			stateMachineCommandType = GameCommandType.LOCAL_WORD_PICKED;
		}
		
		
		if (drawRequest.hasGuessWord()){
//			GameLog.info(session.getSessionId(), "user "+drawRequest.getGuessUserId()+ 
//					" guess "+drawRequest.getGuessWord());
			User guessUser = userManager.findUserById(drawRequest.getGuessUserId());
			session.userGuessWord(guessUser, drawRequest.getGuessWord());						

			if (sessionManager.isAllUserGuessWord(session)){
				stateMachineCommandType = GameCommandType.LOCAL_ALL_USER_GUESS;
			}		
		}				
						
//		if (reason != GameCompleteReason.REASON_NOT_COMPLETE){
//			gameService.fireTurnFinishEvent(session, reason);
//		}
		
		// broast draw data to all other users in the session
		GameNotification.broadcastDrawDataNotification(session, gameEvent, gameEvent.getMessage().getUserId());			
		
		// fire pick word local message
		// drive state machine running
		if (stateMachineCommandType != null){
			GameEvent stateMachineEvent = new GameEvent(
					stateMachineCommandType, 
					session.getSessionId(), 
					gameEvent.getMessage(), 
					channel);
			
			gameService.dispatchEvent(stateMachineEvent);
		}
		
	}

}
