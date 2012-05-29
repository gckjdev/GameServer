package com.orange.gameserver.draw.dao;

import java.util.Date;

public class UserGuessWord {

	final String userId;
	final int guessDifficultLevel;
	String lastWord;
	Date lastDate;
	volatile int finalCoins = 0;
	volatile boolean isCorrect = false;
	volatile int guessTimes = 0;
	
	public UserGuessWord(String userId, int guessDifficultLevel){
		super();
		this.userId = userId;
		if (guessDifficultLevel <= 0){
			this.guessDifficultLevel = 1;
		}
		else{
			this.guessDifficultLevel = guessDifficultLevel;			
		}
	}
	
	public void guess(String newWord, boolean isCorrect, int coins){
		
		if (this.isCorrect){
			// no need to update data again
			return;
		}
		
		this.guessTimes ++;
		this.lastDate = new Date();
		this.isCorrect = isCorrect;
		this.lastWord = newWord;
		this.finalCoins = coins;
	}
	
}
