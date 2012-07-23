package com.orange.gameserver.draw.statemachine;

import com.orange.common.statemachine.Action;
import com.orange.common.statemachine.Condition;
import com.orange.common.statemachine.DecisionPoint;
import com.orange.common.statemachine.StateMachine;
import com.orange.common.statemachine.StateMachineBuilder;
import com.orange.gameserver.draw.action.GameAction;
import com.orange.gameserver.draw.action.GameCondition;
import com.orange.gameserver.draw.statemachine.game.GameStartState;
import com.orange.gameserver.draw.statemachine.game.GameState;
import com.orange.gameserver.draw.statemachine.game.GameStateKey;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class NewGameStateMachineBuilder extends StateMachineBuilder {

	// thread-safe singleton implementation
    private static NewGameStateMachineBuilder builder = new NewGameStateMachineBuilder();
    private static StateMachine stateMachine = builder.buildStateMachine();
    private NewGameStateMachineBuilder(){		
	} 	
    public static NewGameStateMachineBuilder getInstance() {         	
    	return builder; 
    } 
    	
    @Override
	public StateMachine buildStateMachine() {
		StateMachine sm = new StateMachine();
		
		Action initGame = new GameAction.InitGame();
		Action startGame = new GameAction.StartGame();
		Action completeGame = new GameAction.CompleteGame();
		Action selectDrawUser = new GameAction.SelectDrawUser();
		Action kickDrawUser = new GameAction.KickDrawUser();
		Action playGame = new GameAction.PlayGame();
		Action prepareRobot = new GameAction.PrepareRobot();
		Action calculateDrawUserCoins = new GameAction.CalculateDrawUserCoins();
		Action selectDrawUserIfNone = new GameAction.SelectDrawUserIfNone();
		
		Action setOneUserWaitTimer = new GameAction.SetOneUserWaitTimer();
		Action setStartGameTimer = new GameAction.SetStartGameTimer();
		Action setWaitPickWordTimer = new GameAction.SetWaitPickWordTimer();
		Action setDrawGuessTimer = new GameAction.SetDrawGuessTimer();
		Action clearTimer = new GameAction.ClearTimer();
		Action clearRobotTimer = new GameAction.ClearRobotTimer();
		Action broadcastDrawUserChange = new GameAction.BroadcastDrawUserChange();

		Condition checkUserCount = new GameCondition.CheckUserCount();
		
		sm.addState(GameStartState.defaultState)		
			.addAction(initGame)
			.addAction(clearTimer)
			.addEmptyTransition(GameCommandType.LOCAL_DRAW_USER_QUIT)			
			.addEmptyTransition(GameCommandType.LOCAL_ALL_OTHER_USER_QUIT)			
			.addEmptyTransition(GameCommandType.LOCAL_OTHER_USER_QUIT)			
			.addEmptyTransition(GameCommandType.LOCAL_TIME_OUT)			
			.addTransition(GameCommandType.LOCAL_NEW_USER_JOIN, GameStateKey.CHECK_USER_COUNT)
			.addAction(selectDrawUser)
			.addAction(broadcastDrawUserChange);
		
		sm.addState(new GameState(GameStateKey.CHECK_USER_COUNT))
			.setDecisionPoint(new DecisionPoint(checkUserCount){
				@Override
				public Object decideNextState(Object context){
					int userCount = condition.decide(context);
					if (userCount == 0){
						return GameStateKey.CREATE;
					}
					else if (userCount == 1){ // only one user
						return GameStateKey.ONE_USER_WAITING;
					}
					else{ // more than one user
						return GameStateKey.WAIT_FOR_START_GAME;
					}
				}
			});
		
		sm.addState(new GameState(GameStateKey.ONE_USER_WAITING))
			.addAction(setOneUserWaitTimer)
			.addAction(prepareRobot)
			.addTransition(GameCommandType.LOCAL_NEW_USER_JOIN, GameStateKey.CHECK_USER_COUNT)
			.addTransition(GameCommandType.LOCAL_DRAW_USER_QUIT, GameStateKey.CREATE)
			.addTransition(GameCommandType.LOCAL_ALL_OTHER_USER_QUIT, GameStateKey.CREATE)	
			.addTransition(GameCommandType.LOCAL_OTHER_USER_QUIT, GameStateKey.CREATE)
			.addTransition(GameCommandType.LOCAL_TIME_OUT, GameStateKey.KICK_DRAW_USER)	
			.addAction(clearTimer)
			.addAction(clearRobotTimer);
		
		sm.addState(new GameState(GameStateKey.WAIT_FOR_START_GAME))
			.addAction(setStartGameTimer)
			.addTransition(GameCommandType.LOCAL_DRAW_USER_QUIT, GameStateKey.DRAW_USER_QUIT)
			.addTransition(GameCommandType.LOCAL_ALL_OTHER_USER_QUIT, GameStateKey.CHECK_USER_COUNT)	
			.addEmptyTransition(GameCommandType.LOCAL_OTHER_USER_QUIT)
			.addEmptyTransition(GameCommandType.LOCAL_NEW_USER_JOIN)
			.addTransition(GameCommandType.LOCAL_START_GAME, GameStateKey.WAIT_PICK_WORD)
			.addTransition(GameCommandType.LOCAL_TIME_OUT, GameStateKey.KICK_DRAW_USER)	
			.addTransition(GameCommandType.LOCAL_DRAW_USER_CHAT, GameStateKey.WAIT_FOR_START_GAME)	
			.addAction(clearTimer);
		
		sm.addState(new GameState(GameStateKey.DRAW_USER_QUIT))	
			.addAction(selectDrawUser)
			.addAction(broadcastDrawUserChange)			
			.setDecisionPoint(new DecisionPoint(null){
				@Override
				public Object decideNextState(Object context){
					return GameStateKey.CHECK_USER_COUNT;	// goto check user count state directly
				}
			});	
		
		sm.addState(new GameState(GameStateKey.KICK_DRAW_USER))
			.addAction(kickDrawUser)
			.addAction(selectDrawUser)
			.addAction(broadcastDrawUserChange)
			.setDecisionPoint(new DecisionPoint(null){
				@Override
				public Object decideNextState(Object context){
					return GameStateKey.CHECK_USER_COUNT;	// goto check user count state directly
				}
			});
		
		sm.addState(new GameState(GameStateKey.WAIT_PICK_WORD))
			.addAction(startGame)
			.addAction(setWaitPickWordTimer)
			.addTransition(GameCommandType.LOCAL_WORD_PICKED, GameStateKey.DRAW_GUESS)
			.addTransition(GameCommandType.LOCAL_DRAW_USER_QUIT, GameStateKey.COMPLETE_GAME)
			.addTransition(GameCommandType.LOCAL_ALL_OTHER_USER_QUIT, GameStateKey.COMPLETE_GAME)	
			.addEmptyTransition(GameCommandType.LOCAL_OTHER_USER_QUIT)
			.addTransition(GameCommandType.LOCAL_TIME_OUT, GameStateKey.KICK_DRAW_USER)	
			.addAction(clearTimer);
		
		sm.addState(new GameState(GameStateKey.DRAW_GUESS))
			.addAction(setDrawGuessTimer)
			.addAction(playGame)		
			.addTransition(GameCommandType.LOCAL_DRAW_USER_QUIT, GameStateKey.COMPLETE_GAME)
			.addTransition(GameCommandType.LOCAL_ALL_OTHER_USER_QUIT, GameStateKey.COMPLETE_GAME)	
			.addTransition(GameCommandType.LOCAL_ALL_USER_GUESS, GameStateKey.COMPLETE_GAME)
			.addEmptyTransition(GameCommandType.LOCAL_OTHER_USER_QUIT)
			.addEmptyTransition(GameCommandType.LOCAL_DRAW_USER_CHAT)
			.addTransition(GameCommandType.LOCAL_TIME_OUT, GameStateKey.COMPLETE_GAME)				
			.addAction(clearTimer);
		
		sm.addState(new GameState(GameStateKey.COMPLETE_GAME))
			.addAction(calculateDrawUserCoins)
			.addAction(selectDrawUser)
			.addAction(broadcastDrawUserChange)
			.addAction(completeGame)
//			.addAction(sendGameCompleteNotification)			
			.setDecisionPoint(new DecisionPoint(null){
				@Override
				public Object decideNextState(Object context){
					return GameStateKey.CHECK_USER_COUNT;	// goto check user count state directly
				}
			});
			
		
		/*
			// make transition
			.addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_JOIN_GAME, GameStateKey.WAITING)
			
			// no change on state
			.addTransition(GameCommandType.LOCAL_USER_TIME_OUT, GameStateKey.CREATE)
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
			.addTransition(GameCommandType.LOCAL_USER_TIME_OUT, GameStateKey.WAITING)

			// make transition
			.addTransition(GameCommandType.LOCAL_GAME_TURN_COMPLETE, GameStateKey.WAITING)
			.addTransition(GameCommandType.START_GAME_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_FINISH_GAME, GameStateKey.CREATE);
		
		sm.addState(new GamePlayingState(GameStateKey.PLAYING))
			// no change on state
			.addTransition(GameCommandType.JOIN_GAME_REQUEST, GameStateKey.PLAYING)		
			.addTransition(GameCommandType.QUIT_GAME_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_JOIN_GAME, GameStateKey.PLAYING)
			.addTransition(GameCommandType.CHAT_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_USER_TIME_OUT, GameStateKey.PLAYING)
			

			.addTransition(GameCommandType.SEND_DRAW_DATA_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.CLEAN_DRAW_REQUEST, GameStateKey.PLAYING)
			.addTransition(GameCommandType.LOCAL_CHANNEL_DISCONNECT, GameStateKey.PLAYING)
			
			// make transition
			.addTransition(GameCommandType.LOCAL_GAME_TURN_COMPLETE, GameStateKey.WAITING)
			.addTransition(GameCommandType.LOCAL_FINISH_GAME, GameStateKey.CREATE);
		
		sm.addState(GameFinishState.defaultState);
		
		sm.setStartAndFinalState(GameStateKey.CREATE, GameStateKey.FINISH);
		*/
		
		sm.printStateMachine();		
		return sm;
	}

}
