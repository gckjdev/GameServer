package com.orange.gameserver.draw.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.mongodb.BasicDBObject;
import com.orange.common.mongodb.MongoDBClient;
import com.orange.gameserver.db.DrawDBClient;
import com.orange.gameserver.db.service.DrawStorageService;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.gameserver.robot.manager.RobotManager;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCompleteReason;
import com.orange.network.game.protocol.model.GameBasicProtos.PBDraw;
import com.orange.network.game.protocol.model.GameBasicProtos.PBDrawAction;

public class GameTurn {
	
	enum TurnStatus{
		PICK_WORD,
		PLAYING,
		FINISH
	}
	

	final String	wordText;
	final int		wordLevel;			
	final int		round;
	final int		sessionId;
	final int 		language;
	
	TurnStatus status;
	
	GameCompleteReason completeReason = GameCompleteReason.REASON_NOT_COMPLETE;
	
	String drawUserId = null;
	int drawUserCoins = 0;
	ConcurrentMap<String, UserGuessWord> userGuessWordMap = new ConcurrentHashMap<String, UserGuessWord>();
	List<DrawAction> drawActionList = new ArrayList<DrawAction>();

	
	public GameTurn(int sessionId, int round, String word, int level, int language, String currentPlayUserId) {
		this.sessionId = sessionId;
		this.round = round;
		this.wordLevel = level;
		this.wordText = word;
		this.status = TurnStatus.PICK_WORD;
		this.language = language;
		this.drawUserId = currentPlayUserId;
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

	public boolean isAllUserGuessWord(List<String> userIdList) {
//		int guessUserCount = userCount - 1;
//		if (userGuessWordMap.size() < guessUserCount)
//			return false;
//		
//		int correctCount = 0;
		
		for (String userId : userIdList){
			if (userGuessWordMap.containsKey(userId)){
				UserGuessWord guess = userGuessWordMap.get(userId);
				if (guess == null || !guess.isCorrect){
					return false;
				}					
			}
			else{
				return false;
			}
		}
		
		return true;
		
//		for (UserGuessWord guess : userGuessWordMap.values()){
//			if (guess.isCorrect)
//				correctCount ++;
//		}
//		
//		return (correctCount >= guessUserCount);
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
		
		GameLog.info(sessionId, "drawUserCoins set to "+drawUserCoins);
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
		setTurnStatus(TurnStatus.FINISH);
		this.completeReason = reason;
	}
	
	public GameCompleteReason getCompleteReason(){
		return this.completeReason;
	}
	
	public synchronized void setTurnStatus(TurnStatus newStatus){
		this.status = newStatus;
	}

	public synchronized boolean isTurnPlaying() {
		return (status != TurnStatus.FINISH);
	}
	
	public void storeDrawData() {
		

//		PBDraw drawData = PBDrawAction.newBuilder()
		
		if (RobotManager.isRobotUser(drawUserId)){
			GameLog.info(sessionId, "skip store draw data due to robot draw user");
			return;
		}
				
		List<PBDrawAction> pbDrawDataList = new ArrayList<PBDrawAction>();
		for (DrawAction action : drawActionList){
			if (action.actionType == DrawAction.DRAW_ACTION_TYPE_CLEAN){
				PBDrawAction cleanDraw = PBDrawAction.newBuilder().setType(action.actionType).build();
				pbDrawDataList.add(cleanDraw);
			}
			else{
				PBDrawAction draw = PBDrawAction.newBuilder().setType(action.actionType)
					.setColor(action.color)
					.setWidth(action.width)
					.addAllPoints(action.pointList)
					.build();
				pbDrawDataList.add(draw);
			}
		}
		
		PBDraw draw = PBDraw.newBuilder().setUserId(drawUserId)
			.setWord(wordText)
			.setLevel(wordLevel)
			.setLanguage(language)
			.setCreateDate((int)System.currentTimeMillis()/1000)
			.addAllDrawData(pbDrawDataList)
			.build();
		
		// write data here...
		byte[] data = draw.toByteArray();		
		
		
		DrawStorageService.getInstance().storeDraw(sessionId, drawUserId, wordText, wordLevel, 
				language, data);
		
	}
	
	public void appendCleanDrawAction() {
		DrawAction action = new DrawAction();
		drawActionList.add(action);
	}

	public void appendDrawData(List<Integer> pointsList, int color, float width) {
		DrawAction action = new DrawAction(width, color, pointsList);
		drawActionList.add(action);		
	}

}
