package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;

public class User {
	
	final String userId;
	final String nickName;
	final String avatar;
	int currentSessionId = -1;		// TODO change to final or not?
	final Channel channel;
	
	public User(String userId, String nickName, String avatar, Channel channel, int sessionId) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.avatar = avatar;
		this.channel = channel;
		this.setCurrentSessionId(sessionId);
	}
	
	public String getUserId() {
		return userId;
	}
		
	public String getNickName() {
		return nickName;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public synchronized int getCurrentSessionId() {
		return currentSessionId;
	}

	public synchronized void setCurrentSessionId(int currentSessionId) {
		this.currentSessionId = currentSessionId;
	}
}




