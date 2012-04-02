package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameTurn {

	final String	wordText;
	final int		wordLevel;			
	final int		round;
	
	ConcurrentMap<String, UserGuessWord> userGuessWordMap = new ConcurrentHashMap<String, UserGuessWord>();
	List<DrawAction>	drawActionList = new ArrayList<DrawAction>();
	
	public GameTurn(int round, String word, int level) {
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

	public void userGuessWord(String userId, String guessWord){
		if (userId == null || guessWord == null)
			return;
		
		UserGuessWord guess = userGuessWordMap.get(userId);
		if (guess == null){
			guess = new UserGuessWord(userId);
			userGuessWordMap.put(userId, guess);
		}
		
		guess.guess(guessWord, guessWord.equalsIgnoreCase(wordText));
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
}
