package com.orange.gameserver.hit.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.orange.gameserver.hit.dao.GameSession;
import com.orange.gameserver.hit.manager.GameManager;

import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.NewGameRequest;

public class GameService {
	
	GameManager gameManager = new GameManager();
	
	public void handleNewGameRequest(NewGameRequest request){

	}
}
