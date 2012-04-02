package com.orange.gameserver.draw.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
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

	private Object getMatchSessionLock = new Object();

	/*
	private int getMatchedSessionId() {
		GameSessionManager gameSessionManager = GameSessionManager
				.getInstance();
		int joinSessionId = -1;
		joinSessionId = gameSessionManager
				.getRandId(GameSessionManager.SESSION_SET_CANDIDATE);
		if (joinSessionId == -1) {
			joinSessionId = gameSessionManager
					.getRandId(GameSessionManager.SESSION_SET_FREE);
		}
		return joinSessionId;
	}

	private int getMatchedSessionId(User user, boolean useRecentSessions) {
		GameSessionManager gameSessionManager = GameSessionManager
				.getInstance();
		int joinSessionId = -1;
		if (useRecentSessions) {
			int freeSessionIdInList = -1;
			// if can find the candidate session for the session id in the
			// recent session list
			for (Integer sessionId : user.getRecentGameSessionList()) {
				int sessionSymbol = gameSessionManager
						.getSetSymbolBySessionId(sessionId);
				if (sessionSymbol == GameSessionManager.SESSION_SET_CANDIDATE) {
					joinSessionId = sessionId.intValue();
					break;
				} else if (sessionSymbol == GameSessionManager.SESSION_SET_FREE
						&& freeSessionIdInList == -1) {
					freeSessionIdInList = sessionId.intValue();
				}
			}

			// if none of the recent game session is the candidate, the random a
			// candidate
			if (joinSessionId == -1) {
				joinSessionId = gameSessionManager
						.getRandId(GameSessionManager.SESSION_SET_CANDIDATE);
			}

			// if all the candidate game session is full, then find the recent
			// free session
			if (joinSessionId == -1) {
				if (freeSessionIdInList != -1) {
					joinSessionId = freeSessionIdInList;
				} else {
					joinSessionId = gameSessionManager
							.getRandId(GameSessionManager.SESSION_SET_FREE);
				}
			}

		} else {
			// if don't use the recent list
			// find a candidate session not in the list
			joinSessionId = gameSessionManager.getSessionNotInList(
					GameSessionManager.SESSION_SET_CANDIDATE, user
							.getRecentGameSessionList());

			// if can not find a candidate session not in the list, then find a
			// free session not in the list.
			if (joinSessionId == -1) {
				joinSessionId = gameSessionManager.getSessionNotInList(
						GameSessionManager.SESSION_SET_FREE, user
								.getRecentGameSessionList());
			}
		}
		if (joinSessionId != -1) {
			gameSessionManager.adjustGameSession(joinSessionId);
			user.addGameSessionId(joinSessionId);
		}
		logger.info("<GameService>:did find room: " + joinSessionId
				+ " for user: " + user.getUserId());
		gameSessionManager.printSets();
		return joinSessionId;
	}
	
	public int matchGameForUser(String userId, String string, String string2,
			Channel channel, int sessionId) {
		return 0;
	}
	

	public int matchGameForUser(String userId, String nickName, String gameId,
			Channel channel) {
		
		if (userId == null){
			return -1;
		}
		
		boolean useRecentSessions = false;
		synchronized (getMatchSessionLock) {			
			int joinSessionId = getMatchedSessionId();

			if (joinSessionId != -1) {
				GameManager gameManager = GameManager.getInstance();
				GameSession gameSession = gameManager
						.findGameSessionById(joinSessionId);
				gameSession.addUser(userId, nickName, channel);
				GameSessionManager gameSessionManager = GameSessionManager
						.getInstance();
				gameSessionManager.adjustGameSession(joinSessionId);
				
			}

			// TODO code below can be moved out of synchronized lock
			UserManager.getInstance().addOnlineUser(userId, nickName, channel, joinSessionId);
			//ChannelUserManager.getInstance().addChannel(channel);
			ChannelUserManager.getInstance().addUserIntoChannel(channel, userId);

			logger.info("<GameService>:did find room: " + joinSessionId
					+ " for user: " + userId);
			return joinSessionId;
		}
	}
	*/

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

	public void sendResponse(Channel channel, GameMessage response) {
		// TODO Auto-generated method stub

	}

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
		
		Log.info("fire event " + command + ", sessionId = " + sessionId + ", userId = " + userId);
		
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

	private static int EXPIRE_TIME_SECONDS = 120;
	
	public void scheduleGameSessionExpireTimer(final GameSession session) {
		if (session == null)
			return;
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				fireTurnFinishEvent(session);
			}
			
		}, EXPIRE_TIME_SECONDS*1000);
		
		session.setExpireTimer(timer);
	}

	public void fireTurnFinishEvent(GameSession session) {
		GameMessageProtos.GameMessage message = GameMessageProtos.GameMessage.newBuilder()
			.setCommand(GameCommandType.LOCAL_GAME_TURN_COMPLETE)
			.setSessionId(session.getSessionId())
			.setUserId("")
			.setMessageId(0)
			.build();

		GameEvent event = new GameEvent(GameCommandType.LOCAL_GAME_TURN_COMPLETE, 
			session.getSessionId(), message, null);

		dispatchEvent(event);
	}

}
