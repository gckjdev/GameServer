package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEvent extends Event {

	public GameEvent(Object eventKey) {
		super(eventKey);
		// TODO Auto-generated constructor stub
	}

	GameSession  targetSession;
	GameMessage message;
	public GameSession getTargetSession() {
		return targetSession;
	}
	public void setTargetSession(GameSession targetSession) {
		this.targetSession = targetSession;
	}
	public GameMessage getMessage() {
		return message;
	}
	public void setMessage(GameMessage message) {
		this.message = message;
	}
	
	
}
