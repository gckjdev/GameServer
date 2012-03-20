package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTurn {

	String	wordText;
	int		wordLevel;		
	
	int		round;
	
	Map<String, UserGuessWord> userGuessWordMap = new HashMap<String, UserGuessWord>();
	List<DrawAction>	drawActionList = new ArrayList<DrawAction>();
	
	public GameTurn(int round){
		this.round = round;
	}
	
	public void addDrawAction(DrawAction action){
		if (action == null)
			return;
		
		drawActionList.add(action);
	}

	public String getWordText() {
		return wordText;
	}

	public void setWordText(String wordText) {
		this.wordText = wordText;
	}

	public int getWordLevel() {
		return wordLevel;
	}

	public void setWordLevel(int wordLevel) {
		this.wordLevel = wordLevel;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}
	
	public void userGuessWord(String userId, String guessWord){
		if (userId == null || guessWord == null)
			return;
		
		UserGuessWord guess = userGuessWordMap.get(userId);
		if (guess == null){
			guess = new UserGuessWord(userId);
		}
		
		guess.guess(guessWord, guessWord.equals(wordText));
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
