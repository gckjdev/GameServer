package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.State;
import com.orange.gameserver.hit.statemachine.game.GameStartState;
import com.orange.gameserver.hit.statemachine.game.GameStateKey;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;



public class GameSession {
	
	enum SessionStatus{
		INIT,
		WAIT,
		PLAYING		
	};

	protected static final Logger logger = Logger.getLogger("GameSession");
	
	public static final int MAX_USER_PER_GAME_SESSION = 7;
	
	int   sessionId;
	String name;
	String createBy;
	String host;
	Date   createDate;
	State  currentState;
	UserAtGame currentPlayUser = null;
	UserAtGame nextPlayUser = null;
	SessionStatus status = SessionStatus.INIT;
	
	GameTurn currentTurn = null;
	
	
	List<UserAtGame> userList = new CopyOnWriteArrayList<UserAtGame>();
	Object userLock = new Object();

	public GameSession(int sessionId, String gameName, String userId) {
		this.sessionId = sessionId;
		this.name = gameName;
		this.createBy = userId;
		this.host = userId;
		this.createDate = new Date();	
		
		currentState = GameStartState.defaultState;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int id) {
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

	public int getUserCount() {
		return this.userList.size();
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

	private void addUser(UserAtGame userAtGame) {
		if (userAtGame == null)
			return;
		logger.info("add user " + userAtGame.userId);
		userList.add(userAtGame);
	}

	public int addUser(String userId, String nickName, Channel channel) {
		synchronized(userLock){
			for (UserAtGame user : userList){
				if (user.userId.equals(userId)){
					// exist, don't need to add, just update channel
					user.setChannel(channel);
					return userList.size();
				}
			}
			
			if (isUserFull()){
				return userList.size();
			}
			
			UserAtGame userAtGame = new UserAtGame(userId, nickName, channel);
			addUser(userAtGame);		
			if (userList.size() == 1){
				this.host = userId;
			}
							
			return userList.size();
		}
	}

	private boolean isUserFull() {
		return userList.size() >= MAX_USER_PER_GAME_SESSION ? true : false;
	}	
	
	public boolean canUserStartGame(String userId) {
//		if (!userId.equals(host))
//			return false;
		
		logger.info("<canUserStartGame> userId = "+userId);
		for (UserAtGame user : userList){
			if (user.userId.equals(userId))
				return true;
		}
		
		return false;
	}

	public String getCurrentPlayUserId() {
		return (currentPlayUser == null) ? "" : currentPlayUser.userId;
	}

	public String getNextPlayUserId() {
		return (nextPlayUser == null) ? "" : nextPlayUser.userId;
	}

	public void chooseNewHost(int oldHostIndex){
		if (userList.size() == 0){
			this.host = "";
			return;
		}
		
		if (oldHostIndex < 0 || oldHostIndex >= userList.size()){			
			this.host = userList.get(0).userId;
			return;
		}
		else {
			this.host = userList.get(oldHostIndex).userId;
		}
		
		logger.info("set new host = " + host);
	}
		
	public void chooseNewPlayUser() {
		
		// set current play user
		if (currentPlayUser == null){
			if (userList.size() == 0){
				logger.warn("<chooseNewPlayUser> but no user?");
				return;
			}
			else{
				// use the first user
				currentPlayUser = userList.get(0);
			}
		}
		else{
			int index = userList.indexOf(currentPlayUser);
			if (index != -1){
				if (index == userList.size()-1){
					// last user in the list, go to the first user
					currentPlayUser = userList.get(0);
				}
				else{
					// use next user as current player
					currentPlayUser = userList.get(index+1);
				}
			}
			else{
				// current play user not found (maybe removed)
				if (userList.indexOf(nextPlayUser) != -1){
					currentPlayUser = nextPlayUser;
				}
				else if (userList.size() == 0){
					logger.warn("<chooseNewPlayUser> but no user?");
					return;
				}
				else{
					// use the first user
					currentPlayUser = userList.get(0);
				}
			}
				
		}
		
		logger.info("<chooseNewPlayUser> current play user = "+this.getCurrentPlayUserId());
		
		// set next play user
		if (userList.size() < 2){
			nextPlayUser = null;
			return;
		}
		
		// set next play user, it's the next one of current user
		int index = userList.indexOf(currentPlayUser);
		if (index != -1){
			if (index == userList.size()-1){
				// last user in the list, go to the first user
				nextPlayUser = userList.get(0);
			}
			else{
				// use next user as current player
				nextPlayUser = userList.get(index+1);
			}
		}
		
		logger.info("<chooseNewPlayUser> next play user = "+this.getNextPlayUserId());
	}

	@Override
	public String toString() {
		return "GameSession [createBy=" + createBy + ", createDate="
				+ createDate + ", currentPlayUser=" + currentPlayUser
				+ ", currentState=" + currentState + ", host=" + host
				+ ", name=" + name + ", nextPlayUser=" + nextPlayUser
				+ ", sessionId=" + sessionId + ", userList=" + userList + "]\n";
	}

	public boolean isStart() {
		return (status == SessionStatus.PLAYING);
	}

	public void startGame(){
		status = SessionStatus.PLAYING;
	}
	
	public void finishGame(){
		status = SessionStatus.WAIT;
	}

	public void removeUser(String userId) {
		synchronized(userLock){
			UserAtGame userFound = null;
			for (UserAtGame user : userList){
				if (user.getUserId().equals(userId)){
					userFound = user;
					break;
				}
			}
			
			if (userFound != null){
							
				int index = userList.indexOf(userFound);
				if (index != -1){
					userList.remove(index);
				}
				logger.info("remove " + userId + " from session " + sessionId);
	
				if (this.host.equals(userId)){
					// set next user as host
					chooseNewHost(index);
				}
				
				if (currentPlayUser != null && currentPlayUser.equals(userId)){
					// set new current play user and next play user
					this.chooseNewPlayUser();
				}
				
			}
		}
	}

	public void resetGame() {
		status = SessionStatus.INIT;
	}

	public boolean isRoomEmpty() {
		return (userList.size() == 0) ? true : false;
	}
	
	public void startNewTurn(String word, int level){
		if (currentTurn == null){
			currentTurn = new GameTurn(1);
		}
		else{
			currentTurn = new GameTurn(currentTurn.getRound() + 1);
		}
		
		currentTurn.setWordText(word);
		currentTurn.setWordLevel(level);
	}

	public int getCurrentRound() {
		if (currentTurn == null)
			return 1;
		
		return currentTurn.getRound();
	}
}
