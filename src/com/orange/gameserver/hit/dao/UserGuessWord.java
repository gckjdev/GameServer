package com.orange.gameserver.hit.dao;

import java.util.Date;

public class UserGuessWord {

	String userId;
	String lastWord;
	boolean isCorrect = false;
	int guessTimes = 0;
	Date lastDate;
	
	public UserGuessWord(String userId){
		super();
		this.userId = userId;
	}
	
	public void guess(String newWord, boolean isCorrect){
		
		if (this.isCorrect){
			// no need to update data again
			return;
		}
		
		this.guessTimes ++;
		this.lastDate = new Date();
		this.isCorrect = isCorrect;
		this.lastWord = newWord;
	}
	
}
