package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;



public class User {
	
	String userId;
	String nickName;
	int currentSessionId = -1;
	List<Integer> recentGameSessionList;
	Channel channel;
	
	private final int MAX_RECENT_GAMESESSION_COUNT = 5;
	public User(String userId, String nickName, Channel channel, int sessionId) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.channel = channel;
		this.currentSessionId = sessionId;
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
			
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
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

	public int getCurrentSessionId() {
		return currentSessionId;
	}

	public void setCurrentSessionId(int currentSessionId) {
		this.currentSessionId = currentSessionId;
	}
	
	
}




