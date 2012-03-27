package com.orange.gameserver.draw.statemachine;

import com.orange.common.statemachine.State;
import com.orange.common.statemachine.StateMachine;
import com.orange.common.statemachine.StateMachineBuilder;
import com.orange.gameserver.draw.statemachine.game.*;
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
		
		sm.addState(GameStartState.defaultState)
			// make transition
			.addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_JOIN_GAME, GameStateKey.WAITING)
			
			// no change on state
			.addTransition(GameCommandType.LOCAL_CHANNEL_DISCONNECT, GameStateKey.CREATE)
			.addTransition(GameCommandType.CHAT_REQUEST, GameStateKey.CREATE)
			.addTransition(GameCommandType.QUIT_GAME_REQUEST, GameStateKey.CREATE)
			.addTransition(GameCommandType.LOCAL_GAME_TURN_COMPLETE, GameStateKey.CREATE)
			.addTransition(GameCommandType.LOCAL_FINISH_GAME, GameStateKey.CREATE);


		sm.addState(new GameWaitingState(GameStateKey.WAITING))
			// no change on state
			.addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_JOIN_GAME, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_CHANNEL_DISCONNECT, GameStateKey.WAITING)
			.addTransition(GameCommandType.QUIT_GAME_REQUEST, GameStateKey.WAITING)
			.addTransition(GameCommandType.CHAT_REQUEST, GameStateKey.WAITING)

			// make transition
			.addTransition(GameCommandType.START_GAME_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_FINISH_GAME, GameStateKey.CREATE);
		
		sm.addState(new GamePlayingState(GameStateKey.PLAYING))
			// no change on state
			.addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.PLAYING)		
			.addTransition(GameCommandType.QUIT_GAME_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_JOIN_GAME, GameStateKey.PLAYING)
			.addTransition(GameCommandType.CHAT_REQUEST, GameStateKey.PLAYING)

			.addTransition(GameCommandType.SEND_DRAW_DATA_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.CLEAN_DRAW_REQUEST, GameStateKey.PLAYING)
			
			// make transition
			.addTransition(GameCommandType.LOCAL_CHANNEL_DISCONNECT, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_GAME_TURN_COMPLETE, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_FINISH_GAME, GameStateKey.CREATE);
		
		sm.addState(GameFinishState.defaultState);
		
		sm.setStartAndFinalState(GameStateKey.CREATE, GameStateKey.FINISH);
		
		sm.printStateMachine();		
		return sm;
	}

}
