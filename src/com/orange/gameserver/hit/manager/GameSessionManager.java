package com.orange.gameserver.hit.manager;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.orange.gameserver.hit.dao.GameSession;

public class GameSessionManager {
	
	protected static final Logger logger = Logger.getLogger("GameSession");

	
	//use three sets to classify the game sessions
	ConcurrentHashSet<Integer> candidateGameSessionSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> freeGameSessionSet = new ConcurrentHashSet<Integer>();
	ConcurrentHashSet<Integer> fullGameSessionSet = new ConcurrentHashSet<Integer>();
	
	public static int SESSION_SET_FREE = 0;
	public static int SESSION_SET_CANDIDATE = 1;
	public static int SESSION_SET_FULL = 2;
	public static int SESSION_SET_ILLEGAL = -1;

	
	private ConcurrentHashSet<Integer> getSetByGameSessionSize(int size) {
		if (size == GameSession.MAX_USER_PER_GAME_SESSION) {
			return fullGameSessionSet;
		}
		if (size >= GameSession.MAX_USER_PER_GAME_SESSION - 2) {
			return candidateGameSessionSet;
		}
		if (size >= 0) {
			return freeGameSessionSet;
		}
		return null;
	}
	
    private static GameSessionManager manager = new GameSessionManager();     
    private GameSessionManager(){
    	logger.info("<GameSessionManager> init");
    	GameManager gameManager = GameManager.getInstance();
    	for (int i = 1; i <= gameManager.getGameSessionSize(); i++) {
			adjustGameSession(i);
		}
    	printSets();
	} 	    
    public static GameSessionManager getInstance() { 
    	return manager; 
    } 
    
    public void printSets() {
    	for (int i = 0; i < 3; i++) {
    		ConcurrentHashSet<Integer> set = getGameSessionSetBySymbol(i);
        	logger.info("<Set " + i + ">:" + set);
		}
	}
    
    
    public ConcurrentHashSet<Integer> getGameSessionSetBySymbol(int symbol)
    {
    	if (symbol == SESSION_SET_CANDIDATE) {
			return candidateGameSessionSet;
		}
    	if (symbol == SESSION_SET_FREE) {
			return freeGameSessionSet;
		}
    	if (symbol == SESSION_SET_FULL) {
			return fullGameSessionSet;
		}
    	return null;
    }
    
    
    private int getSymbolByGameSessionSet (ConcurrentHashSet<Integer> set) {
		if (set == candidateGameSessionSet) {
			return SESSION_SET_CANDIDATE;
		}
		if (set == freeGameSessionSet) {
			return SESSION_SET_FREE;
		}
		if (set == fullGameSessionSet) {
			return SESSION_SET_FULL;
		}
		return SESSION_SET_ILLEGAL;
	}
    

    public int getGameSessionSetSizeBySymbol(int symbol)
    {
    	ConcurrentHashSet<Integer> set = getGameSessionSetBySymbol(symbol);
    	if (set != null) {
			return set.size();
		}
    	return 0;
    }
    
    public int getRandGameSessionId(int symbol) {
		ConcurrentHashSet<Integer>set = getGameSessionSetBySymbol(symbol);
		if (set != null && !set.isEmpty()) {
			for(Integer integer : set)
			{
				return integer.intValue();
			}
		}
		return -1;
	}
    
    public ConcurrentHashSet<Integer> getGameSessionSetById (int sessionId) {
		Integer integer = new Integer(sessionId);
		if (candidateGameSessionSet.contains(integer)) {
			return candidateGameSessionSet;
		}
		if (freeGameSessionSet.contains(integer))
		{
			return freeGameSessionSet;
		}
		if (fullGameSessionSet.contains(integer)) {
			return fullGameSessionSet;
		}
		return null;
	}
    
    public int getGameSessionSetSymbolById (int sessionId) {
    	return getSymbolByGameSessionSet(getGameSessionSetById(sessionId));
    }
        
    public int getGameSessionNotInList(int sessionSetSymbol, List<Integer> list) {
    	ConcurrentHashSet<Integer> set = getGameSessionSetBySymbol(sessionSetSymbol);
		for (Integer sessionId : set) {
			if(!list.contains(sessionId))
			{
				return sessionId;
			}
		}
		return -1;
	}
    
	public void adjustGameSession(int gameSessionId) {
		GameSession session = GameManager.getInstance().findGameSessionById(gameSessionId);
		adjustGameSession(session);
//		Integer integer = new Integer(gameSessionId);
//		ConcurrentHashSet<Integer> oldSet = getGameSessionSetById(gameSessionId);
//		GameManager gameManager = GameManager.getInstance();
//		GameSession session = gameManager.findGameSessionById(gameSessionId);
//		int size = session.getUserCount();
//		ConcurrentHashSet<Integer> newSet = getSetByGameSessionSize(size);
//		if (newSet == null) {
//			return;
//		}
//		if (oldSet == null) {
//			newSet.add(integer);
//		}else{
//			if (oldSet == newSet) {
//				return;
//			}
//			oldSet.remove(integer);
//			newSet.add(integer);
//		}
	}
	
	public void adjustGameSession(GameSession gameSession) {
		if (gameSession == null) {
			return;
		}
		Integer integer = gameSession.getSessionId();
		ConcurrentHashSet<Integer> oldSet = getGameSessionSetById(integer.intValue());
		int size = gameSession.getUserCount();
		ConcurrentHashSet<Integer> newSet = getSetByGameSessionSize(size);
		
		if (newSet == null) {
			return;
		}
		
		int index = getSymbolByGameSessionSet(newSet);
		logger.info("put " + integer + " into " + index);
		
		if (oldSet == null) {
			newSet.add(integer);
		}else{
			if (oldSet == newSet) {
				return;
			}
			oldSet.remove(integer);
			newSet.add(integer);
		}
	}
}
