package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



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


	
}
