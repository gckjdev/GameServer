package com.orange.gameserver.draw.dao;

import java.util.Date;

public class UserGuessWord {

	String userId;
	String lastWord;
	boolean isCorrect = false;
	int guessTimes = 0;
	Date lastDate;
	int finalCoins = 0;
	
	public UserGuessWord(String userId){
		super();
		this.userId = userId;
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
