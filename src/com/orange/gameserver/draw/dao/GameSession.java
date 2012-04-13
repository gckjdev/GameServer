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
		GameLog.info(sessionId, "start game, set status to" + status);
	}
	
	public void finishGame(){
		status = SessionStatus.WAIT;
		GameLog.info(sessionId, "finish game, set status to" + status);
	}
	
	public void resetGame() {
		status = SessionStatus.INIT;
		this.resetExpireTimer();
		GameLog.info(sessionId, "reset game, set status to" + status);
	}

	public void waitForPlay() {
		status = SessionStatus.WAIT;
		this.resetExpireTimer();
		GameLog.info(sessionId, "wait for play, set status to" + status);
	}
	
	public void startNewTurn(String word, int level){
		if (currentTurn == null){
			currentTurn = new GameTurn(sessionId, 1, word, level);
		}
		else{
			currentTurn = new GameTurn(sessionId, currentTurn.getRound() + 1, word, level);
		}		
		GameLog.info(sessionId, "start new game turn "+currentTurn.getRound(), "word=" + word);
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


	public void userGuessWord(String guessUserId, String guessWord) {
		if (currentTurn == null)
			return;
		
		GameLog.info(sessionId, "user " + guessUserId + " guess " + guessWord);			
		currentTurn.userGuessWord(guessUserId, guessWord);
	}

	public synchronized boolean isCurrentPlayUser(String userId) {
		if (currentPlayUser == null || userId == null)
			return false;
				
		return currentPlayUser.userId.equals(userId);
	}

	public synchronized void setCurrentPlayUser(User user){
		this.currentPlayUser = user;
		GameLog.info(sessionId, "current play user is set to "+user);					
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
		
		if (currentPlayUser == null){
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

	
	
}
