package com.orange.gameserver.hit.service;

import org.jboss.netty.channel.MessageEvent;

import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class GameSessionRequestHandler extends AbstractRequestHandler {

	public GameSessionRequestHandler(MessageEvent messageEvent) {
		super(messageEvent);
	}

	@Override
	public void handleRequest(GameMessage message) {
		gameService.dispatchEvent(this.toGameEvent(gameMessage));
	}

}
