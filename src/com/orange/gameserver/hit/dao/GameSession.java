package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.orange.network.game.protocol.GameProtos;
import com.orange.network.game.protocol.GameProtos.Game;

public class GameSession {

	String gameId;
	String name;
	String createBy;
	String host;
	Date   createDate;
	GameSessionState state = GameSessionState.CREATE;
	List<UserAtGame> userList = new ArrayList<UserAtGame>();	

	public GameSession(String gameId, String gameName, String userId) {
		this.gameId = gameId;
		this.name = gameName;
		this.createBy = userId;
		this.host = userId;
		this.createDate = new Date();		
	}

	public Game toGame() {
		GameProtos.Game newGame = GameProtos.Game.newBuilder()
			.setCreateBy(createBy)
			.setCreateTime((int)(createDate.getTime()/1000))
			.setGameId(gameId)
			.setName(name)
			.setHost(host)
			.addUsers(host)
			.build();		

		return newGame;
	}
	
}
