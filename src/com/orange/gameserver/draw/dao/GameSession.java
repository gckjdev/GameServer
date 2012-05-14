package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.common.statemachine.State;
import com.orange.gameserver.draw.manager.GameSessionUserManager;
import com.orange.gameserver.draw.manager.UserManager;
import com.orange.gameserver.draw.server.GameService;
import com.orange.gameserver.draw.statemachine.game.GameStartState;
import com.orange.gameserver.draw.statemachine.game.GameStateKey;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.model.GameBasicProtos;
import com.orange.network.game.protocol.model.GameBasicProtos.PBGameUser;



public class GameSession {
	
	enum SessionStatus{
		INIT,
		WAIT,
		PLAYING		
	};
	
	final int   sessionId;
	final String friendRoomId;
	final String name;
	final String createBy;
	final String host;
	final Date   createDate;
	State  currentState;
	User currentPlayUser = null;
	SessionStatus status = SessionStatus.INIT;	

	GameTurn currentTurn = null;	
	Timer expireTimer;
	
	public GameSession(int sessionId, String gameName, String userId) {
		this.sessionId = sessionId;
		this.name = gameName;
		this.createBy = userId;
		this.host = userId;
		this.createDate = new Date();	
		this.friendRoomId = null;
		
		currentState = GameStartState.defaultState;
	}
	
	public GameSession(int sessionId, String roomName, String userId, String roomId) {
		this.sessionId = sessionId;
		this.name = roomName;
		this.createBy = userId;
		this.host = userId;
		this.createDate = new Date();	
		this.friendRoomId = roomId;
		
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

	public synchronized String getCurrentPlayUserId() {
		return (currentPlayUser == null) ? "" : currentPlayUser.userId;
	}

	@Override
//	public String toString() {
//		return "GameSession [createBy=" + createBy + ", createDate="
//				+ createDate + ", currentPlayUser=" + currentPlayUser
//				+ ", currentState=" + currentState + ", host=" + host
//				+ ", name=" + name
//				+ ", sessionId=" + sessionId + "]\n";
//	}
	public String toString() {
		return String.format("[%010d]", sessionId);
	}
	
	public boolean isStart() {
		return (status == SessionStatus.PLAYING);
	}

	public void startGame(){
		status = SessionStatus.PLAYING;
		GameLog.info(sessionId, "start game, set status to " + status);
	}
	
	public void finishGame(){
		status = SessionStatus.WAIT;
		clearStartExpireTimer();
		GameLog.info(sessionId, "finish game, set status to " + status);
	}
	
	public void resetGame() {
		status = SessionStatus.INIT;
		this.resetExpireTimer();
		clearStartExpireTimer();
		GameLog.info(sessionId, "reset game, set status to " + status);
	}

	public void waitForPlay() {
		status = SessionStatus.WAIT;
		this.resetExpireTimer();
		GameLog.info(sessionId, "wait for play, set status to " + status);
	}
	
	public synchronized void startNewTurn(String word, int level, int language){
		if (currentTurn == null){
			currentTurn = new GameTurn(sessionId, 1, word, level, language, this.currentPlayUser);
		}
		else{
			currentTurn.storeDrawData();
			currentTurn = new GameTurn(sessionId, currentTurn.getRound() + 1, word, level, language, this.currentPlayUser);
		}		
		GameLog.info(sessionId, "start new game turn "+currentTurn.getRound(), "word=" + word);
		
//		this.clearStartExpireTimer();
	}
	
	public synchronized boolean isGameTurnPlaying(){
		if (currentTurn == null){			
			return (this.status == SessionStatus.PLAYING);
		}
		
		return currentTurn.isTurnPlaying();
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
			GameLog.info(sessionId, "cancel & clear expire timer");			
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


	public void userGuessWord(User user, String guessWord) {
		if (currentTurn == null || user == null)
			return;
		
		String guessUserId = user.getUserId();
		GameLog.info(sessionId, "user " + guessUserId + " guess " + guessWord);			
		currentTurn.userGuessWord(user, guessWord);
	}

	public synchronized boolean isCurrentPlayUser(String userId) {
		if (currentPlayUser == null || userId == null)
			return false;
				
		return currentPlayUser.userId.equals(userId);
	}

	Timer startExpireTimer = null;
	static final int DEFAULT_START_EXPIRE_TIMER = 32*1000;
	
	public void clearStartExpireTimer(){
		if (startExpireTimer != null){
			GameLog.info(sessionId, "Clear start expire timer");			
			startExpireTimer.cancel();
			startExpireTimer = null;
		}
	}
	
	public void scheduleStartExpireTimer(final String userId){
				
		clearStartExpireTimer();		

		GameLog.info(sessionId, "Scheule start expire timer on userId="+userId);
		startExpireTimer = new Timer();
		startExpireTimer.schedule(new TimerTask(){

			@Override
			public void run() {
				try{
					GameLog.info(sessionId, "Fire start expire timer on userId="+userId);
					User user = UserManager.getInstance().findUserById(userId);
					if (user == null){
						// user already disconnect?
						GameService.getInstance().fireUserTimeOutEvent(sessionId, userId, null);					
					}
					else{
						GameService.getInstance().fireUserTimeOutEvent(sessionId, userId, user.getChannel());
					}
				}
				catch (Exception e){
					GameLog.error(sessionId, e, "Exception while fire start expire timer on userId="+userId);
				}
				
				startExpireTimer = null;
			}
			
		}, DEFAULT_START_EXPIRE_TIMER);
	}
		
//	public void startExpireTimerIfNeeded() {
//		if (this.currentPlayUser != null && startExpireTimer == null){
//			scheduleStartExpireTimer(currentPlayUser.getUserId());
//		}
//	}

	public void resetStartExpireTimer() {
		if (this.currentPlayUser != null){
			scheduleStartExpireTimer(currentPlayUser.getUserId());
		}
	}
	
	public void startStartExpireTimerIfNeeded() {
		if (startExpireTimer == null){
			scheduleStartExpireTimer(currentPlayUser.getUserId());
		}
	}
	
	
	public synchronized void setCurrentPlayUser(User user){
		this.currentPlayUser = user;
		GameLog.info(sessionId, "current play user is set to "+user);		
		
		if (user != null){
			// set a start timer here
			scheduleStartExpireTimer(user.getUserId());
		}
	}

	public boolean isAllUserGuessWord(List<String> userIdList) {
		if (currentTurn == null){
			GameLog.warn(sessionId, "call isAllUserGuessWord but current turn is null?");
			return false;
		}
		
		return currentTurn.isAllUserGuessWord(userIdList);
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
		
		if (currentPlayUser == null){
			return;
		}
		
		currentTurn.calculateDrawUserCoins(this.currentPlayUser);
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

	public void completeTurn(GameCompleteReason completeReason) {
		if (this.currentTurn == null)
			return;
		
		GameLog.info(sessionId, "<completeTurn> on session " + sessionId + " reason=" + completeReason);
		currentTurn.completeTurn(completeReason);
	}

	public GameCompleteReason getCompleteReason() {
		if (this.currentTurn == null)
			return GameCompleteReason.REASON_NOT_COMPLETE;

		return currentTurn.completeReason;
	}

	public User getCurrentPlayUser() {
		return this.currentPlayUser;
	}

	private ScheduledFuture timeOutFuture = null;
	
	public void setTimeOutFuture(ScheduledFuture future) {
		if (timeOutFuture != null){
			timeOutFuture.cancel(false);
			timeOutFuture = null;
		}
		
		timeOutFuture = future;
	}

	public void clearTimeOutFuture(ScheduledFuture future) {
		if (timeOutFuture != null){
			timeOutFuture.cancel(false);
			timeOutFuture = null;
		}		
	}

	ScheduledFuture<Object> inviteRobotTimer = null;
		
	public void setRobotTimeOutFuture(ScheduledFuture<Object> future) {
		if (inviteRobotTimer != null){
			inviteRobotTimer.cancel(false);
			inviteRobotTimer = null;
		}
		
		inviteRobotTimer = future;
	}

	public void clearRobotTimer() {
		if (inviteRobotTimer != null){
			inviteRobotTimer.cancel(false);
			inviteRobotTimer = null;
		}		
	}

	public void appendDrawData(List<Integer> pointsList, int color, float width) {
		if (currentTurn == null)
			return;

		currentTurn.appendDrawData(pointsList, color, width);
	}

	public void appendCleanDrawAction() {
		if (currentTurn == null)
			return;
		
		currentTurn.appendCleanDrawAction();
	}

	public String getFriendRoomId() {
		return this.friendRoomId;
	}

	
}
