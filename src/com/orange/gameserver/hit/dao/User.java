package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.List;



public class User {
	String userId;
	String nickName;
	List<Integer> recentGameSessionList;
	private final int MAX_RECENT_GAMESESSION_COUNT = 5;
	public User(String userId, String nickName) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.recentGameSessionList = new ArrayList<Integer>(MAX_RECENT_GAMESESSION_COUNT);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public List<Integer> getRecentGameSessionList() {
		return recentGameSessionList;
	}
	public void addGameSessionId(int gameSessionId) {
		Integer integer = new Integer(gameSessionId);
		if(this.recentGameSessionList.size() >= MAX_RECENT_GAMESESSION_COUNT) {
			this.recentGameSessionList.remove(MAX_RECENT_GAMESESSION_COUNT - 1);
		}
		this.recentGameSessionList.add(0, integer);
	}
	
	
}




