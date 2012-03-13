package com.orange.gameserver.hit.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEvent extends Event {

	public GameEvent(Object eventKey) {
		super(eventKey);
		// TODO Auto-generated constructor stub
	}

	GameMessage	message;
}
