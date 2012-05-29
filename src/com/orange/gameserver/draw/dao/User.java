package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;

import com.orange.network.game.protocol.model.GameBasicProtos.PBSNSUser;

public class User {
	
	final String userId;
	final String nickName;
	final String avatar;
	final Channel channel;
	final boolean gender;
	final boolean isRobot;
	final int guessDifficultLevel;
	final String location;
	final List<PBSNSUser> snsUser;

	volatile int currentSessionId = -1;		
	volatile boolean isPlaying = false;
	
	public User(String userId, String nickName, String avatar, boolean gender, 
			String location, List<PBSNSUser> snsUser,
			Channel channel, int sessionId, int guessLevel) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.avatar = avatar;
		this.channel = channel;
		this.gender = gender;
		this.setCurrentSessionId(sessionId);
		this.isRobot = false;
		this.location = location;
		this.snsUser = snsUser;
		if (guessLevel <= 0)
			this.guessDifficultLevel = 1;
		else
			this.guessDifficultLevel = guessLevel;
	}
	
	public User(String userId, String nickName, String avatar, boolean gender,
			String location, List<PBSNSUser> snsUser,
			Channel channel, int sessionId, boolean isRobot, int guessLevel) {
		super();
		this.userId = userId;
		this.nickName = nickName;
		this.avatar = avatar;
		this.channel = channel;
		this.gender = gender;
		this.setCurrentSessionId(sessionId);
		this.isRobot = isRobot;
		this.location = location;
		this.snsUser = snsUser;
		if (guessLevel <= 0)
			this.guessDifficultLevel = 1;
		else
			this.guessDifficultLevel = guessLevel;
	}		

	public String getLocation() {
		return location;
	}

	public List<PBSNSUser> getSnsUser() {
		return snsUser;
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

	public int getCurrentSessionId() {
		return currentSessionId;
	}

	public void setCurrentSessionId(int currentSessionId) {
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

	public boolean isPlaying(){
		return this.isPlaying;
	}
	
	public void setPlaying(boolean value){
		this.isPlaying = value;
	}	
}




