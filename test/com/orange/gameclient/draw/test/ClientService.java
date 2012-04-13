package com.orange.gameclient.draw.test;

import org.jboss.netty.channel.MessageEvent;

import com.orange.gameclient.draw.test.dao.ClientUser;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;

public class ClientService {

	private static int messageId = 1;
	private static ClientService instance = new ClientService();
	void sendStartRequst(MessageEvent messageEvent, ClientUser user){
		//uid sid
	}
	
	private ClientService () {
		super();
	}
	
	
	
	
	public static ClientService getInstanceClientService(){
		if (instance == null) {
			instance = new ClientService();
		}
		return instance;
	}
	
	void sendJoinGameRequest(ClientUser user){
		//uid nick avatar gender sid sset

		JoinGameRequest request = null;
		GameMessage message = null;
		if (user.getSessionId() > 0) {
//			request = JoinGameRequest.newBuilder().
//			setUserId(user.getUserId()).
//			setNickName(user.getNickName()).
//			setGender(user.getGender()).
//			setAvatar(user.getAvatarUrl()).setGameId("").
//			setSessionToBeChange(user.getSessionId()).build();
//			
//			message = GameMessage.newBuilder().
//			setCommand(GameCommandType.JOIN_GAME_REQUEST).
//			setJoinGameRequest(request).
//			setMessageId(messageId ++).
//			setUserId(user.getUserId()).
//			setSessionId(user.getSessionId()).build();
			
		}else{

			request = JoinGameRequest.newBuilder().
			setUserId(user.getUserId()).
			setNickName(user.getNickName()).
			setGender(user.getGender()).
			setAvatar(user.getAvatarUrl()).
			setGameId("").
			build();			
			
			message = GameMessage.newBuilder().			
			setMessageId(messageId ++).
			setCommand(GameCommandType.JOIN_GAME_REQUEST).
			setJoinGameRequest(request).
			build();
		}
		
		user.getChannel().write(message);
		
	}
	
	void sendRunawayRequest(ClientUser user){
		
	}
//	- (void)sendJoinGameRequest:(NSString*)userId 
//    nickName:(NSString*)nickName 
//      avatar:(NSString*)avatar
//      gender:(BOOL)gender
//   sessionId:(int)currentSessionId
//excludeSessionSet:(NSSet*)excludeSessionSet;
//
//- (void)sendStartGameRequest:(NSString*)userId sessionId:(long)sessionId;
//
//- (void)sendDrawDataRequest:(NSString*)userId 
//   sessionId:(long)sessionId 
//   pointList:(NSArray*)pointList 
//       color:(int)color
//       width:(float)width;
//
//- (void)sendCleanDraw:(NSString*)userId 
//sessionId:(long)sessionId;
//
//- (void)sendStartDraw:(NSString*)userId 
//sessionId:(long)sessionId
//  word:(NSString*)word 
// level:(int)level
//language:(int)language;
//
//- (void)sendProlongGame:(NSString*)userId 
//sessionId:(long)sessionId;
//
//- (void)sendQuitGame:(NSString*)userId 
//sessionId:(long)sessionId;
//
//- (void)sendAskQuickGame:(NSString*)userId 
//sessionId:(long)sessionId;
//
//- (void)sendGuessWord:(NSString*)guessWord
//guessUserId:(NSString*)guessUserId
//userId:(NSString*)userId
//sessionId:(long)sessionId;
//
//
//- (void)sendRankGameResult:(int)rank
//     userId:(NSString*)userId
//  sessionId:(long)sessionId
//      round:(int)round;
//
//- (int)stringToRank:(NSString*)rankString;
//- (NSString*)rankToString:(int)rank;

}
