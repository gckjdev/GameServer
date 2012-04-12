package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;

public class GameTurn {
	

	final String	wordText;
	final int		wordLevel;			
	final int		round;
	final int		sessionId;
	
	GameCompleteReason completeReason = GameCompleteReason.REASON_NOT_COMPLETE;
	
	String drawUserId = null;
	int drawUserCoins = 0;
	ConcurrentMap<String, UserGuessWord> userGuessWordMap = new ConcurrentHashMap<String, UserGuessWord>();
	List<DrawAction>	drawActionList = new ArrayList<DrawAction>();

	
	public GameTurn(int sessionId, int round, String word, int level) {
		this.sessionId = sessionId;
		this.round = round;
		this.wordLevel = level;
		this.wordText = word;
	}

	public void addDrawAction(DrawAction action){
		if (action == null)
			return;
		
		drawActionList.add(action);
	}

	public String getWordText() {
		return wordText;
	}

	public int getWordLevel() {
		return wordLevel;
	}

	public int getRound() {
		return round;
	}
	
	private int calcBasicCoins(int level){
		switch (level){
		case 1:
			return 3;
		case 2:
			return 4;
		case 3:
		default:
			return 5;
		}
	}

	public void userGuessWord(String userId, String guessWord){
		if (userId == null || guessWord == null)
			return;
		
		UserGuessWord guess = userGuessWordMap.get(userId);
		if (guess == null){
			guess = new UserGuessWord(userId);
			userGuessWordMap.put(userId, guess);
		}
		
		boolean isCorrect = guessWord.equalsIgnoreCase(wordText);
		int finalCoins = 0;
		if (isCorrect && !guess.isCorrect){
			// calculate gain points
			int basicCoins = calcBasicCoins(wordLevel);
			int correctCount = 0;
			Collection<UserGuessWord> list = userGuessWordMap.values();
			for (UserGuessWord uw : list){
				if (uw.isCorrect){
					correctCount ++;
				}
				
				if (correctCount >= 2)
					break;
			}
			
			finalCoins = basicCoins;
			switch (correctCount){
			case 0:
				break;
			case 1:
				finalCoins -= 1;
				break;
			default:
				finalCoins -= 2;
				break;
			}
			
			GameLog.info(sessionId, "<userGuessWord> correct, gain coins = " + finalCoins);
		}

		guess.guess(guessWord, isCorrect, finalCoins);
		
	}

	public boolean isAllUserGuessWord(int userCount) {
		int guessUserCount = userCount - 1;
		if (userGuessWordMap.size() < guessUserCount)
			return false;
		
		int correctCount = 0;
		for (UserGuessWord guess : userGuessWordMap.values()){
			if (guess.isCorrect)
				correctCount ++;
		}
		
		return (correctCount >= guessUserCount);
	}

	public void calculateDrawUserCoins(String userId) {
		this.drawUserId  = userId;
		Collection<UserGuessWord> list = userGuessWordMap.values();
		int correctCount  = 0;
		for (UserGuessWord uw : list){
			if (uw.isCorrect)
				correctCount ++;
			if (correctCount >= 1)
				break;
		}
		
		if (correctCount >= 1){
			this.drawUserCoins = this.calcBasicCoins(wordLevel);			
		}
		else{
			this.drawUserCoins = 0;
		}
	}

	public int getUserFinalCoins(String userId) {
		if (drawUserId != null && drawUserId.equals(userId)){
			return drawUserCoins;
		}			
		
		UserGuessWord uw = userGuessWordMap.get(userId);
		if (uw == null)
			return 0;
		else
			return uw.finalCoins;
				
	}
	
	public void completeTurn(GameCompleteReason reason){
		this.completeReason = reason;
	}
	
	public GameCompleteReason getCompleteReason(){
		return this.completeReason;
	}
}
