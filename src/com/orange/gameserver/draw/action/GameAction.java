package com.orange.gameserver.draw.action;

import com.orange.common.statemachine.Action;
import com.orange.gameclient.draw.test.dao.SessionManager;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.service.GameNotification;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;

public class GameAction{
	
	

	public static class StartGame implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.startGame();
		}

	}

	public static final GameSessionUserManager sessionUserManager = GameSessionUserManager.getInstance();
	public static final GameSessionManager sessionManager = GameSessionManager.getInstance();

	public static class CompleteGame implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;

			session.calculateDrawUserCoins();
			sessionManager.adjustSessionSetForTurnComplete(session);			
			sessionUserManager.clearUserPlaying(session);
			session.completeTurn();	// TODO set right reason here
			
			GameNotification.broadcastNotification(session, null, GameCommandType.GAME_TURN_COMPLETE_NOTIFICATION_REQUEST);
		}

	}

	public static class ClearTimer implements Action {

		@Override
		public void execute(Object context) {
			// TODO Auto-generated method stub

		}

	}

	public static class SetWaitPickWordTimer implements Action {

		@Override
		public void execute(Object context) {
			// TODO Auto-generated method stub

		}

	}

	public static class KickDrawUser implements Action {

		@Override
		public void execute(Object context) {
			// TODO Auto-generated method stub

		}

	}

	public static class SetStartGameTimer implements Action {

		@Override
		public void execute(Object context) {
			// TODO Auto-generated method stub

		}

	}

	public static class SetOneUserWaitTimer implements Action {

		@Override
		public void execute(Object context) {
			// TODO Auto-generated method stub

		}

	}

	public static class SelectDrawUser implements Action {

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			GameSessionManager.getInstance().selectCurrentPlayer(session);
		}

	}

	public static class InitGame implements Action{

		@Override
		public void execute(Object context) {
			GameSession session = (GameSession)context;
			session.resetGame();
		}
		
	}

}
