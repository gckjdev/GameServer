package com.orange.gameserver.hit.dao;

import java.util.ArrayList;
import java.util.List;

public class GameTurn {

	String	wordText;
	int		wordLevel;		
	
	int		round;
	
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
	
	
}
