package com.orange.gameserver.draw.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.orange.common.statemachine.StateMachine;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.service.HandlerUtils;
import com.orange.gameserver.draw.statemachine.GameStateMachineBuilder;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameResultCode;


public class GameWorkerThread extends Thread {
	
	protected static final Logger logger = Logger.getLogger(GameWorkerThread.class.getName());
	
	static final StateMachine stateMachine = GameStateMachineBuilder.getInstance().buildStateMachine();
	GameSessionManager gameManager = GameSessionManager.getInstance();
	GameService gameService = GameService.getInstance();
	PriorityBlockingQueue<GameEvent> queue = new PriorityBlockingQueue<GameEvent>(4096);
	AtomicInteger stopFlag = new AtomicInteger(0);

	public int threadHashKey;
	
	@Override
	public void run() {
		
		logger.info("Thread " + threadHashKey + " is running...");
		
		while (stopFlag.intValue() == 0){
			GameEvent event = null;
			try {				
				event = queue.take();
				GameSession session = gameManager.findGameSessionById(event.getTargetSession());
				if (session == null){
					// no session available for this event?
					
					GameLog.warn(event.getTargetSession(), "process event but session id not found ");
					HandlerUtils.sendErrorResponse(event, GameResultCode.ERROR_NO_SESSION_ID);
					continue;
				}
				
				if (session.getCurrentState().validateEvent(event, session) != 0){
					// donnot send back request here since validate event will make it
					// argument, where to send the response???
					continue;
				}
				
				 com.orange.common.statemachine.State nextState = 
					 stateMachine.nextState(session.getCurrentState(), event, session);
				 
				 if (nextState == null){
					 // incorrect message event?
					 HandlerUtils.sendErrorResponse(event, GameResultCode.ERROR_NEXT_STATE_NOT_FOUND);					 
					 continue;
				 }
				  
				 session.setCurrentState(nextState);

			} catch (Exception e) {
				logger.error("catch exception while handle event, exception = "+e.toString(), e);
				if (event != null){
					HandlerUtils.sendErrorResponse(event, GameResultCode.ERROR_SYSTEM_EXCEPTION);
				}
			}					
		}
		
		logger.info("Thread " + threadHashKey + " is stop...");

		
		
	}
	
	public void stopGraceful(){
		logger.info("Thread " + threadHashKey + " try stop gracefully");
		stopFlag.incrementAndGet();
	}

	public void putEvent(GameEvent gameEvent) {
		try {
			queue.put(gameEvent);
		} catch (Exception e) {
			logger.error("<putEvent> but catch exception " + e.toString(), e);
		}		
	}

//	public void putEventHead(GameEvent gameEvent) {
//		try {
//			queue.put(gameEvent);
//		} catch (Exception e) {
//			logger.error("<putEventHead> but catch exception " + e.toString(), e);
//		}		
//	}
	
	
}
