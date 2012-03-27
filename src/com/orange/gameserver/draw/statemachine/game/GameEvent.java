package com.orange.gameserver.draw.statemachine.game;

import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.Event;
import com.orange.gameserver.draw.dao.GameSession;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameEvent extends Event implements Comparable {

	public static final int HIGH = 1;
	public static final int MEDIUM = 2;
	public static final int LOW = 3;
	
	int priority = MEDIUM;
	
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

	public void setPriority(int p){
		this.priority = p;
	}
	
	@Override
	public int compareTo(Object arg0) {
		GameEvent e = (GameEvent)arg0;		
		return (this.priority - e.priority);
	}
	
	
}
