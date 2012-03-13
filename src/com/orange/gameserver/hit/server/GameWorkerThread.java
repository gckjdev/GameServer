package com.orange.gameserver.hit.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.service.GameService;
import com.orange.gameserver.hit.statemachine.game.GameEvent;

public class GameWorkerThread extends Thread {
	
	static ConcurrentHashMap<Integer, GameWorkerThread> workerThreads;
	static int numberOfWorkerThread;
	
	GameService gameService = GameService.getInstance();
	LinkedBlockingQueue<GameEvent> queue = new LinkedBlockingQueue<GameEvent>();
	AtomicInteger stopFlag = new AtomicInteger(0);

	@Override
	public void run() {
		
		while (stopFlag.intValue() == 0){
			try {
				GameEvent event;
				event = queue.take();
				GameSession session = event.getTargetSession();
				if (session == null){
					// no session available for this event?
					continue;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}					
		}
		
		
	}
	
	public void stopGraceful(){
		stopFlag.incrementAndGet();
	}
	
	public static void createWorkerThreads(int numberOfThread){
		numberOfWorkerThread = numberOfThread;
		workerThreads  = new ConcurrentHashMap<Integer, GameWorkerThread>();
		for (int i=0; i<numberOfThread; i++){
			GameWorkerThread worker = new GameWorkerThread();
			workerThreads.put(Integer.valueOf(i), worker);	
			worker.start();
		}
	}
	
}
