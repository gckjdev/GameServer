package com.orange.gameserver.draw.dao;

public class DrawGameSession extends GameSession {

	final byte[] drawingData;
	
	public DrawGameSession(int sessionId, String gameName, String userId) {
		super(sessionId, gameName, userId);
		// TODO Auto-generated constructor stub
		
		drawingData = null;
	}

}
