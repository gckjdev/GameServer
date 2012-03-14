package com.orange.gameserver.hit.statemachine.game;

import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.hit.dao.GameSession;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEvent extends Event {

	public GameEvent(Object eventKey) {
		super(eventKey);
	}

	public GameEvent(Object key, int sessionId,
			GameMessage message, Channel c) {
		
		super(key);		
		this.targetSessionId = sessionId;
		this.message = message;
		this.channel = c;
	}

	int  targetSessionId;
	GameMessage message;
	Channel channel;
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public int getTargetSession() {
		return targetSessionId;
	}
	public void setTargetSession(int id) {
		this.targetSessionId = id;
	}
	public GameMessage getMessage() {
		return message;
	}
	public void setMessage(GameMessage message) {
		this.message = message;
	}
	
	
}
