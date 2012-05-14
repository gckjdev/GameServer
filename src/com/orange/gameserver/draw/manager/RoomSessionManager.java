package com.orange.gameserver.draw.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.orange.gameserver.draw.utils.GameLog;

public class RoomSessionManager {

	Map<String, Integer> roomSessionMap = new ConcurrentHashMap<String, Integer>();
	AtomicInteger roomIndex = new AtomicInteger(GameSessionManager.FRIEND_GAME_SESSION_INDEX);
	
	// thread-safe singleton implementation
    private static RoomSessionManager manager = new RoomSessionManager();         
    private RoomSessionManager(){		
	}    
    public static RoomSessionManager getInstance() { 
    	return manager; 
    } 

    public void addRoomSession(String roomId){
    	if (roomId == null)
    		return;
    	
    	int sessionId = roomIndex.getAndIncrement();
    	roomSessionMap.put(roomId, Integer.valueOf(sessionId));
    	
    	GameLog.info(sessionId, "<addRoomSession> roomId="+roomId);
    }

    public void removeRoomSession(String roomId, int sessionId){
    	roomSessionMap.remove(roomId);    	
    	GameLog.info(sessionId, "<removeRoomSession> roomId="+roomId);
    }
    
    public int getSessionIdByRoom(String roomId){
    	Integer sessionId = roomSessionMap.get(roomId);
    	if (sessionId == null)
    		return -1;
    	else
    		return sessionId.intValue();
    }
    
    
}
