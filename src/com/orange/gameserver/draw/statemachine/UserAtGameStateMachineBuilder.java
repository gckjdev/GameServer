package com.orange.gameserver.draw.statemachine;

import com.orange.common.statemachine.StateMachine;
import com.orange.common.statemachine.StateMachineBuilder;

public class UserAtGameStateMachineBuilder extends StateMachineBuilder {

	private UserAtGameStateMachineBuilder(){		
	} 
	
    private static UserAtGameStateMachineBuilder builder = new UserAtGameStateMachineBuilder(); 

    public static UserAtGameStateMachineBuilder getInstance() { 
    	return builder; 
    } 
	
	@Override
	public StateMachine buildStateMachine() {
		// TODO Auto-generated method stub
		return null;
	}

}
