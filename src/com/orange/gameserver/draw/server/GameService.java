package com.orange.gameserver.draw.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cassandra.cli.CliParser.newColumnFamily_return;
import org.apache.cassandra.thrift.Cassandra.system_add_column_family_args;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.DrawGameSession;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.manager.ChannelUserManager;
import com.orange.gameserver.draw.manager.GameSessionManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.statemachine.game.GameEvent;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.message.GameMessageProtos;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

import com.sun.java_cup.internal.runtime.Symbol;

public class GameService {

	protected static final Logger logger = Logger.getLogger(GameService.class
			.getName());

	
	
	ConcurrentHashMap<Integer, GameWorkerThread> workerThreads = new ConcurrentHashMap<Integer, GameWorkerThread>();
	int numberOfWorkerThread = 20;

	AtomicInteger messageIdIndex = new AtomicInteger(0);

	GameSessionManager gameManager = GameSessionManager.getInstance();

	// thread-safe singleton implementation
	private static GameService defaultService = new GameService();

	private GameService() {
	}

	public static GameService getInstance() {
		return defaultService;
	}

	public ConcurrentHashMap<Integer, GameWorkerThread> createWorkerThreads(
			int numberOfThread) {
		numberOfWorkerThread = numberOfThread;
		for (int i = 0; i < numberOfThread; i++) {
			GameWorkerThread worker = new GameWorkerThread();
			workerThreads.put(Integer.valueOf(i), worker);
			worker.threadHashKey = i;
			worker.start();
			logger.info("Create & Start Worker Thread " + i);
		}

		return workerThreads;
	}

	public void dispatchEvent(GameEvent gameEvent) {
		long assignWorkerThreadIndex = hash(gameEvent.getTargetSession());
		Integer key = Integer.valueOf((int) assignWorkerThreadIndex);
		if (workerThreads.containsKey(key)) {
			GameWorkerThread worker = workerThreads.get(key);
			worker.putEvent(gameEvent);
		}
	}
	
	private long hash(long targetSession) {
		return (targetSession + 31) % numberOfWorkerThread;
	}

//	public void sendResponse(Channel channel, GameMessage response) {
//		// TODO Auto-generated method stub
//
//	}

	public int generateMessageId() {
		return messageIdIndex.incrementAndGet();
	}

	public void fireAndDispatchEvent(GameCommandType command,
			int sessionId, String userId) {

		fireAndDispatchEvent(command, sessionId, userId, GameEvent.MEDIUM);				
	}
	
	public void fireAndDispatchEventHead(GameCommandType command,
			int sessionId, String userId) {

		fireAndDispatchEvent(command, sessionId, userId, GameEvent.HIGH);	
	}

	public void fireAndDispatchEvent(GameCommandType command,
			int sessionId, String userId, int priority) {
		
		GameLog.info(sessionId, "fire event " + command + ", userId = " + userId);
		
		String userIdForMessage = userId;
		if (userId == null){
			userIdForMessage = "";
		}
		
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(command)
			.setSessionId(sessionId)
			.setUserId(userIdForMessage)
			.setMessageId(0)
			.build();
		
		GameEvent event = new GameEvent(command, sessionId, message, null);		
		event.setPriority(priority);
		dispatchEvent(event);
	}

	private static int EXPIRE_TIME_SECONDS = 60;
	
	public void scheduleGameSessionExpireTimer(final GameSession session) {
		if (session == null)
			return;
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				GameLog.info(session.getSessionId(), "expired timer is triggered");
				fireTurnFinishEvent(session, GameCompleteReason.REASON_EXPIRED);
			}
			
		}, EXPIRE_TIME_SECONDS*1000);
		
		GameLog.info(session.getSessionId(), "schedule expired timer after "+ EXPIRE_TIME_SECONDS);
		session.setExpireTimer(timer);
	}

	public void fireTurnFinishEvent(GameSession session, GameCompleteReason reason) {
		
		if (session.isGameTurnPlaying() == false){
			GameLog.warn(session.getSessionId(), "<fireTurnFinishEvent> but game turn is not in PLAY");
			return;
		}
		
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.LOCAL_GAME_TURN_COMPLETE)
			.setSessionId(session.getSessionId())
			.setMessageId(0)
			.setCompleteReason(reason)
			.build();

		GameLog.info(session.getSessionId(), "fire LOCAL_GAME_TURN_COMPLETE event due to "+reason);
		GameEvent event = new GameEvent(GameCommandType.LOCAL_GAME_TURN_COMPLETE, 
			session.getSessionId(), message, null);

		dispatchEvent(event);
	}
	
//	public void fireTimeOutEvent(final GameSession session) {
//		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
//			.setCommand(GameCommandType.LOCAL_GAME_TIME_OUT)
//			.setSessionId(session.getSessionId())
//			.setMessageId(0)
//			.build();
//
//		GameLog.info(session.getSessionId(), "fire LOCAL_GAME_TIME_OUT event");
//		GameEvent event = new GameEvent(GameCommandType.LOCAL_GAME_TIME_OUT, 
//			session.getSessionId(), message, null);
//
//		dispatchEvent(event);
//	}

	public void fireUserTimeOutEvent(int sessionId, String userId, final Channel channel) {
		GameLog.info(sessionId, "fire LOCAL_USER_TIME_OUT event for user "+userId);
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.LOCAL_USER_TIME_OUT)
			.setSessionId(sessionId)
			.setUserId(userId)
			.setMessageId(0)
			.build();

//		GameLog.info(sessionId, "step 2, fire LOCAL_USER_TIME_OUT event for user "+userId);
		GameEvent event = new GameEvent(GameCommandType.LOCAL_USER_TIME_OUT, 
			sessionId, message, channel);
	
		dispatchEvent(event);
		
	}
	
	

}
