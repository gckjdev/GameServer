package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;

public class User {
	
	final String userId;
	final String nickName;
	final String avatar;
	final Channel channel;
	final boolean gender;

	int currentSessionId = -1;		// TODO change to final or not?
	
	public User(String userId, String nickName, String avatar, boolean gender, Channel channel, int sessionId) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.avatar = avatar;
		this.channel = channel;
		this.gender = gender;
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
	
	public boolean getGender(){
		return gender;
	}

	public synchronized int getCurrentSessionId() {
		return currentSessionId;
	}

	public synchronized void setCurrentSessionId(int currentSessionId) {
		this.currentSessionId = currentSessionId;
	}

	public String getAvatar() {
		return this.avatar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [nickName=" + nickName
				+ ", userId=" + userId + "]";
	}
	
	
	
	
}




