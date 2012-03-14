package com.orange.gameserver.hit.service;

import com.orange.gameserver.hit.statemachine.game.GameEvent;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;

public class HandlerUtils {

	public static void sendResponse(GameEvent gameEvent, GameMessage response) {
		if (gameEvent == null || response == null || gameEvent.getChannel() == null)
			return;
		
		gameEvent.getChannel().write(response);
	}


	public static GameCommandType getResponseCommandByRequest(GameCommandType requestCommand){
		return GameCommandType.valueOf(requestCommand.getNumber() + 1);
	}
}
