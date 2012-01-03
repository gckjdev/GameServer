package com.orange.gameserver.hit.statemachine;

import com.orange.common.statemachine.StateMachine;
import com.orange.common.statemachine.StateMachineBuilder;

public class GameStateMachineBuilder extends StateMachineBuilder {

	// thread-safe singleton implementation
    private static GameStateMachineBuilder builder = new GameStateMachineBuilder(); 
    private GameStateMachineBuilder(){		
	} 	
    public static GameStateMachineBuilder getInstance() { 
    	return builder; 
    } 
	
	@Override
	public StateMachine buildStateMachine() {
		// TODO Auto-generated method stub
		return null;
	}

}
