package com.orange.gameserver.hit.statemachine;

import com.orange.common.statemachine.State;
import com.orange.common.statemachine.StateMachine;
import com.orange.common.statemachine.StateMachineBuilder;
import com.orange.gameserver.hit.statemachine.game.*;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class GameStateMachineBuilder extends StateMachineBuilder {

	// thread-safe singleton implementation
    private static GameStateMachineBuilder builder = new GameStateMachineBuilder();
    private static StateMachine stateMachine = builder.buildStateMachine();
    private GameStateMachineBuilder(){		
	} 	
    public static GameStateMachineBuilder getInstance() {         	
    	return builder; 
    } 
    	
    @Override
	public StateMachine buildStateMachine() {
		StateMachine sm = new StateMachine();
		
		sm.addState(GameStartState.defaultState).
			addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.WAITING);

		sm.addState(new GameWaitingState(GameStateKey.WAITING)).
			addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.WAITING);
		
//			
//		sm.addState(new MyState(MyStateKey.STATE_GAME_WAIT)).
//			addTransition(MyEventKey.EVENT_GAME_START, MyStateKey.STATE_GAME_ONGOING).
//			addTransition(MyEventKey.EVENT_GAME_TERMINATE, MyStateKey.STATE_GAME_FINISH);
//		
//		sm.addState(new MyState(MyStateKey.STATE_GAME_ONGOING)).
//			addTransition(MyEventKey.EVENT_GAME_COMPLETE, MyStateKey.STATE_GAME_FINISH).
//			addTransition(MyEventKey.EVENT_GAME_TERMINATE, MyStateKey.STATE_GAME_FINISH);
//
		sm.addState(GameFinishState.defaultState);
		
		sm.setStartAndFinalState(GameStateKey.CREATE, GameStateKey.FINISH);
		
		sm.printStateMachine();		
		return sm;
	}

}
