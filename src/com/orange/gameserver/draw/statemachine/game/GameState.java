package com.orange.gameserver.draw.statemachine.game;

import com.orange.common.statemachine.Event;
import com.orange.common.statemachine.State;

public class GameState extends State {

	public GameState(Object stateId) {
		super(stateId);
	}
	
	@Override
	public void enterAction(Event event, Object context){		
	}

}
