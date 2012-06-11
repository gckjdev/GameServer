package com.orange.gameserver.draw.action;

import com.orange.common.statemachine.Condition;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionUserManager;

public class GameCondition {

	public static class CheckUserCount implements Condition {

		@Override
		public int decide(Object context) {
			GameSession session = (GameSession)context;			
			return GameSessionUserManager.getInstance().getSessionUserCount(session.getSessionId());
		}
		
	}
}
