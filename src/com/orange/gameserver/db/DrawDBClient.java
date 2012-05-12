package com.orange.gameserver.db;

import com.orange.common.mongodb.MongoDBClient;

public class DrawDBClient {

	public static final String DB_NAME = "game";
	
	public static final String T_DRAW = "draw";
	public static final String T_GUESS = "guess";

	public static final String F_USER_ID = "user_id";
	public static final String F_NICK_NAME = "nick_name";
	public static final String F_AVATAR = "avatar";
	public static final String F_WORD = "word";
	public static final String F_LEVEL = "level";
	public static final String F_LANGUAGE = "lang";
	public static final String F_CREATE_DATE = "create_date";
	public static final String F_DRAW_DATA = "data";
	public static final String F_DRAW_DATA_LEN = "data_len";
	public static final String F_RANDOM = "random";
	public static final String F_VIEW_USER_LIST = "view_users";
	public static final String F_GUESS_WORD_LIST = "guess_list";
	
	MongoDBClient mongoClient = new MongoDBClient(DB_NAME);
	
	// thread-safe singleton implementation
    private static DrawDBClient client = new DrawDBClient();     
    private DrawDBClient(){		
	} 	    
    public static DrawDBClient getInstance() { 
    	return client; 
    } 
    
    public MongoDBClient getMongoClient(){
    	return mongoClient;
    }
    
}
