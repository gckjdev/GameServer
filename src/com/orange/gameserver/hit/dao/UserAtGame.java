package com.orange.gameserver.hit.dao;

import org.jboss.netty.channel.Channel;

public class UserAtGame {

	String userId;
	String nickName;
	Channel channel;
	
	GameSession gameSession;
	
	UserAtGameState state = UserAtGameState.CREATE;
	
	public UserAtGame(String userId, String nickName, Channel channel) {
		this.userId = userId;
		this.nickName = nickName;
		this.channel = channel;
	}	
}
