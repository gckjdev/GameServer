package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.statemachine.game.GameStartState;
import com.orange.gameserver.draw.statemachine.game.GameStateKey;
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
	
	final int   sessionId;
	final String name;
	final String createBy;
	final String host;
	final Date   createDate;
	State  currentState;
	User currentPlayUser = null;
	SessionStatus status = SessionStatus.INIT;	

	GameTurn currentTurn = null;	
	Timer expireTimer;
	
	
//	List<UserAtGame> userList = new CopyOnWriteArrayList<UserAtGame>();
//	Object userLock = new Object();

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

	public String getName() {
		return name;
	}

	public String getCreateBy() {
		return createBy;
	}

	public String getHost() {
		return "";
	}

	public Date getCreateDate() {
		return createDate;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

//	public List<UserAtGame> getUserList() {
//		return userList;
//	}
//
//	public int getUserCount() {
//		return this.userList.size();
//	}
//	
//	public void setUserList(List<UserAtGame> userList) {
//		this.userList = userList;
//	}

//	public List<PBGameUser> usersToPBUsers() {
//		List<PBGameUser> list = new ArrayList<PBGameUser>();
//		for (UserAtGame user : userList){
//			GameBasicProtos.PBGameUser gameUser = GameBasicProtos.PBGameUser.newBuilder()
//																				.setUserId(user.userId)																				
//																				.setNickName(user.nickName)
//																				.setAvatar(user.avatar)
//																				.build();
//			list.add(gameUser);
//		}
//		return list;
//	}

//	private void addUser(UserAtGame userAtGame) {
//		if (userAtGame == null)
//			return;
//		logger.info("add user " + userAtGame.userId);
//		userList.add(userAtGame);
//	}

//	public int addUser(String userId, String nickName, String avatar, Channel channel) {
//		synchronized(userLock){
//			for (UserAtGame user : userList){
//				if (user.userId.equals(userId)){
//					// exist, don't need to add, just update channel
//					user.setChannel(channel);
//					return userList.size();
//				}
//			}
//			
//			if (isUserFull()){
//				return userList.size();
//			}
//			
//			UserAtGame userAtGame = new UserAtGame(userId, nickName, avatar, channel);
//			addUser(userAtGame);	
//			
//			// update current play user and next play user
//			// updatePlayUser();			
//			
//			// update host
//			if (userList.size() == 1){
//				this.host = userId;
//				this.currentPlayUser = userAtGame;
//			}
//							
//			return userList.size();
//		}
//	}

	// TODO update current play user
	
//	private boolean isUserFull() {
//		return userList.size() >= MAX_USER_PER_GAME_SESSION ? true : false;
//	}	
	
//	public boolean canUserStartGame(String userId) {
////		if (!userId.equals(host))
////			return false;
//		
//		logger.info("<canUserStartGame> userId = "+userId);
//		for (UserAtGame user : userList){
//			if (user.userId.equals(userId))
//				return true;
//		}
//		
//		return false;
//	}

	public synchronized String getCurrentPlayUserId() {
		return (currentPlayUser == null) ? "" : currentPlayUser.userId;
	}



//	public void chooseNewHost(int oldHostIndex){
//		if (userList.size() == 0){
//			this.host = "";
//			return;
//		}
//		
//		if (oldHostIndex < 0 || oldHostIndex >= userList.size()){			
//			this.host = userList.get(0).userId;
//			return;
//		}
//		else {
//			this.host = userList.get(oldHostIndex).userId;
//		}
//		
//		logger.info("set new host = " + host);
//	}
	
//	private void updatePlayUser(){
//		int userCount = userList.size();
//		if (userCount >= 2){
//			if (currentPlayUser == null || nextPlayUser == null){
//				this.chooseNewPlayUser();
//			}
//			return;
//		}
//		
//		if (userCount == 1){
//			currentPlayUser = userList.get(0);
//			return;
//		}
//		
//		return;
//	}
		
//	public void chooseNewPlayUser() {
//		
//		// set current play user
//		if (currentPlayUser == null){
//			if (userList.size() == 0){
//				logger.warn("<chooseNewPlayUser> but no user?");
//				return;
//			}
//			else{
//				// use the first user
//				currentPlayUser = userList.get(0);
//			}
//		}
//		else{
//			int index = userList.indexOf(currentPlayUser);
//			if (index != -1){
//				if (index == userList.size()-1){
//					// last user in the list, go to the first user
//					currentPlayUser = userList.get(0);
//				}
//				else{
//					// use next user as current player
//					currentPlayUser = userList.get(index+1);
//				}
//			}
//			else{
//				// current play user not found (maybe removed)
//				if (userList.indexOf(nextPlayUser) != -1){
//					currentPlayUser = nextPlayUser;
//				}
//				else if (userList.size() == 0){
//					logger.warn("<chooseNewPlayUser> but no user?");
//					return;
//				}
//				else{
//					// use the first user
//					currentPlayUser = userList.get(0);
//				}
//			}
//				
//		}
//		
//		logger.info("<chooseNewPlayUser> current play user = "+this.getCurrentPlayUserId());
//		
//		// set next play user
//		if (userList.size() < 2){
//			nextPlayUser = null;
//			return;
//		}
//		
//		// set next play user, it's the next one of current user
//		int index = userList.indexOf(currentPlayUser);
//		if (index != -1){
//			if (index == userList.size()-1){
//				// last user in the list, go to the first user
//				nextPlayUser = userList.get(0);
//			}
//			else{
//				// use next user as current player
//				nextPlayUser = userList.get(index+1);
//			}
//		}
//		
//		logger.info("<chooseNewPlayUser> next play user = "+this.getNextPlayUserId());
//	}

	@Override
	public String toString() {
		return "GameSession [createBy=" + createBy + ", createDate="
				+ createDate + ", currentPlayUser=" + currentPlayUser
				+ ", currentState=" + currentState + ", host=" + host
				+ ", name=" + name
				+ ", sessionId=" + sessionId + "]\n";
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
	
//	public UserAtGame findUserById(String userId) {
//		synchronized(userLock){
//			for (UserAtGame user : userList){
//				if (user.getUserId().equals(userId)){
//					return user;
//				}
//			}			
//			
//			return null;
//		}
//	}


//	public void removeUser(String userId) {
//		synchronized(userLock){
//			UserAtGame userFound = null;
//			for (UserAtGame user : userList){
//				if (user.getUserId().equals(userId)){
//					userFound = user;
//					break;
//				}
//			}
//			
//			if (userFound != null){
//							
//				int index = userList.indexOf(userFound);
//				if (index != -1){
//					userList.remove(index);
//				}
//				logger.info("remove " + userId + " from session " + sessionId);
//	
//				if (this.host.equals(userId)){
//					// set next user as host
//					chooseNewHost(index);
//				}
//				
////				if (currentPlayUser != null && currentPlayUser.equals(userId)){
////					// set new current play user and next play user
////					// this.chooseNewPlayUser();
////				}
//				
//			}
//		}
//	}

	public void resetGame() {
		status = SessionStatus.INIT;
		this.resetExpireTimer();
	}

	public void waitForPlay() {
		status = SessionStatus.WAIT;
		this.resetExpireTimer();
	}
	
	public void startNewTurn(String word, int level){
		if (currentTurn == null){
			currentTurn = new GameTurn(1, word, level);
		}
		else{
			currentTurn = new GameTurn(currentTurn.getRound() + 1, word, level);
		}		
	}

	public int getCurrentRound() {
		if (currentTurn == null)
			return 1;
		
		return currentTurn.getRound();
	}

	public String getCurrentGuessWord() {
		if (currentTurn == null)
			return "";
		
		return currentTurn.getWordText();
	}

	public void resetExpireTimer(){
		if (this.expireTimer != null){
			this.expireTimer.cancel();
			this.expireTimer = null;
		}		
	}
	
	public void setExpireTimer(Timer timer) {
		if (this.expireTimer != null){
			this.expireTimer.cancel();
			this.expireTimer = null;
		}
		
		this.expireTimer = timer;
	}

//	public boolean isTurnFinish() {
//		// all users guess the word
//		int userCount = userList.size();
//		if (userCount == 1)
//			return false; // TODO only one user, here for test only
//		
//		return (currentTurn.isAllUserGuessWord(userCount));
//	}

	public void userGuessWord(String guessUserId, String guessWord) {
		if (currentTurn == null)
			return;
		
		currentTurn.userGuessWord(guessUserId, guessWord);
	}

	public synchronized boolean isCurrentPlayUser(String userId) {
		if (currentPlayUser == null || userId == null)
			return false;
				
		return currentPlayUser.userId.equals(userId);
	}

	public synchronized void setCurrentPlayUser(User user){
		this.currentPlayUser = user;
	}

	public boolean isAllUserGuessWord(int userCount) {
		if (currentTurn == null)
			return true;
		
		return currentTurn.isAllUserGuessWord(userCount);
	}

	public int getCurrentGuessUserCoins(String userId) {
		if (currentTurn == null)
			return 0;
		
		UserGuessWord uw = currentTurn.userGuessWordMap.get(userId);
		if (uw == null)
			return 0;
		
		return uw.finalCoins;
	}

	public void calculateDrawUserCoins() {
		if (currentTurn == null){
			return;
		}
		
		currentTurn.calculateDrawUserCoins(this.currentPlayUser.userId);
	}
	
	public int getDrawUserCoins(){
		if (currentTurn == null){
			return 0;
		}

		return currentTurn.drawUserCoins;
	}

	public int getCurrentUserGainCoins(String userId) {
		if (currentTurn == null)
			return 0;
				
		return currentTurn.getUserFinalCoins(userId);
	}
	
}
