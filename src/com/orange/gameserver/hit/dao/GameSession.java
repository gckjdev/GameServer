package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.orange.common.statemachine.State;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;



public class GameSession {

	long   sessionId;
	String name;
	String createBy;
	String host;
	Date   createDate;
	State  currentState;
	List<UserAtGame> userList = new ArrayList<UserAtGame>();	

	public GameSession(long sessionId, String gameName, String userId) {
		this.sessionId = sessionId;
		this.name = gameName;
		this.createBy = userId;
		this.host = userId;
		this.createDate = new Date();		
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long id) {
		this.sessionId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public List<UserAtGame> getUserList() {
		return userList;
	}

	public void setUserList(List<UserAtGame> userList) {
		this.userList = userList;
	}

	public List<PBGameUser> usersToPBUsers() {
		List<PBGameUser> list = new ArrayList<PBGameUser>();
		for (UserAtGame user : userList){
			GameBasicProtos.PBGameUser gameUser = GameBasicProtos.PBGameUser.newBuilder()
																				.setUserId(user.userId)																				
																				.setNickName(user.nickName)
																				.build();
			list.add(gameUser);
		}
		return list;
	}

	public void addUser(UserAtGame userAtGame) {
		if (userAtGame == null)
			return;
		
		userList.add(userAtGame);
	}


	
}
