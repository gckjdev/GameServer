package com.orange.gameserver.draw.dao;

import org.jboss.netty.channel.Channel;

public class UserAtGame {

	final String userId;
	final String nickName;
	final Channel channel;
	final String avatar;
	
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

	public String getNickName() {
		return nickName;
	}

	public Channel getChannel() {
		return channel;
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
