package com.orange.gameserver.draw.dao;

import org.jboss.netty.channel.Channel;

public class UserAtGame {

	String userId;
	String nickName;
	Channel channel;
	String avatar;
	
	GameSession gameSession;
	
	UserAtGameState state = UserAtGameState.CREATE;
	
	public UserAtGame(String userId, String nickName, String avatar, Channel channel) {
		this.userId = userId;
		this.nickName = nickName;
		this.channel = channel;
		this.avatar = avatar;
	}

	public String getUserId() {
		return userId;
	}		

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
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

	public GameSession getGameSession() {
		return gameSession;
	}

	public void setGameSession(GameSession gameSession) {
		this.gameSession = gameSession;
	}

	public UserAtGameState getState() {
		return state;
	}

	public void setState(UserAtGameState state) {
		this.state = state;
	}	
	
	
}
