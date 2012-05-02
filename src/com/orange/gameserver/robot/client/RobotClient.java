package com.orange.gameserver.robot.client;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.orange.common.utils.RandomUtil;
import com.orange.gameclient.draw.test.dao.ClientUser;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.server.DrawGameServer;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.GeneralNotification;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;
import com.orange.network.game.protocol.message.GameMessageProtos.SendDrawDataRequest;

public class RobotClient implements Runnable {

	private static final int MIN_SESSION_USER_COUNT = 4;
	final int sessionId;
	final String userId;
	final String nickName;
	final boolean gender;
	final String userAvatar;
	final int robotIndex;
	
	enum ClientState{
		WAITING,
		PICK_WORD,
		PLAYING
	};
	
	// game session running data
	ConcurrentHashMap<String, User> userList = new ConcurrentHashMap<String, User>();
	ClientState state = ClientState.WAITING;	
	String currentPlayUserId = null;
	int round = -1;
	String word = null;
	int level = 0;
	int language = 0;
	int guessCount = 0;
	
	// simulation
	Timer guessWordTimer;
	
	// connection information
	ChannelFuture future;
	ClientBootstrap bootstrap;
	Channel channel;

	// message
	AtomicInteger messageId = new AtomicInteger(1);
	
	public RobotClient(String userId, String nickName, String avatar, boolean gender, int sessionId, int index){
		this.sessionId = sessionId;
		this.userAvatar = avatar;
		this.userId = userId;
		this.gender = gender;
		this.nickName = nickName;
		this.robotIndex = index;
	}
			
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void run(){
        
        String host = "127.0.0.1";
        int port = DrawGameServer.getPort();
        int THREAD_NUM = 1;

        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newFixedThreadPool(THREAD_NUM),
                        Executors.newFixedThreadPool(THREAD_NUM)));
        
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new RobotClientPipelineFactory(this));

        // Start the connection attempt.
        future = bootstrap.connect(new InetSocketAddress(host, port));
        
        // Wait until the connection is closed or the connection attempt fails.
        future.getChannel().getCloseFuture().awaitUninterruptibly();
        future = null;
        
        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();		        
        bootstrap = null;        
	}
	
	void send(GameMessage msg){
		if (channel != null && channel.isWritable()){
			GameLog.info(sessionId, "robot "+nickName+ " send "+msg.getCommand());
			channel.write(msg);
		}
	}
	
	void sendJoinGameRequest() {

		JoinGameRequest request = null;
		GameMessage message = null;
			
		request = JoinGameRequest.newBuilder().setUserId(userId)
				.setNickName(nickName)
				.setGender(gender).
				setAvatar(userAvatar)
				.setIsRobot(true)
				.setTargetSessionId(sessionId)
				.setGameId("").build();

		message = GameMessage.newBuilder().setMessageId(messageId.getAndIncrement())
				.setCommand(GameCommandType.JOIN_GAME_REQUEST)
				.setJoinGameRequest(request).build();
		
		send(message);
	}

	public void disconnect() {
		GameLog.info(sessionId, "Robot " + nickName + " Disconnect");
		if (channel != null){
			if (channel.isConnected()){
				channel.disconnect();
			}

			channel.close();
			channel = null;
		}
	}
		
	public void removeUserByUserId(String userIdForRemove) {
		userList.remove(userIdForRemove);
	}

	public int sessionUserCount() {
		return userList.size();
	}

	public void sendQuitGameRequest() {
		disconnect();
	}

	public int getClientIndex() {
		return robotIndex;
	}

	public void stopClient() {
		
		disconnect();
		
		if (future != null){		
			future.cancel();
		}
		
        // Shut down thread pools to exit.
		if (bootstrap != null){
			bootstrap.releaseExternalResources();
		}
	}

	public ClientState getState() {
		return state;
	}

	public void setState(ClientState state) {
		this.state = state;
	}

	public void updateByNotification(GeneralNotification notification) {
		
		if (notification == null){
			return;
		}
		
		if (notification.hasCurrentPlayUserId()){
			this.setCurrentPlayUserId(notification.getCurrentPlayUserId());			
		}
		
		if (notification.hasNewUserId()){
			this.addNewUser(notification.getNewUserId(),
					notification.getNickName(),
					notification.getUserAvatar(),
					notification.getUserGender());
		}
		
		if (notification.hasQuitUserId()){
			removeUserByUserId(notification.getQuitUserId());
		}
		
	}

	private void addNewUser(String newUserId, String nickName2,
			String userAvatar2, boolean userGender) {

		if (newUserId == null){
			return;
		}
		
		User user = new User(newUserId, nickName2, userAvatar2, userGender, null, sessionId);
		userList.put(newUserId, user);
	}

	private void setCurrentPlayUserId(String userId) {
		this.currentPlayUserId = userId;
	}

	public boolean canQuitNow() {
		if (this.sessionUserCount() >= MIN_SESSION_USER_COUNT){
			return true;
		}
		else{
			return false;
		}
	}

	public void updateTurnData(GeneralNotification notification) {
		if (notification == null)
			return;
		
		if (notification.hasRound())
			this.round = notification.getRound();
		
		if (notification.hasWord())
			this.word = notification.getWord();
		
		if (notification.hasLevel())
			this.level = notification.getLevel();
		
		if (notification.hasLanguage())
			this.language = notification.getLanguage();
		
	}
	
	public final void sendGuessWord(String guessWord){
		SendDrawDataRequest request = SendDrawDataRequest.newBuilder().setGuessWord(guessWord)
			.setGuessUserId(userId)
			.build();
		
		GameMessage message = GameMessage.newBuilder().setCommand(GameCommandType.SEND_DRAW_DATA_REQUEST)
			.setMessageId(messageId.getAndIncrement())
			.setUserId(userId)
			.setSessionId(sessionId)
			.setSendDrawDataRequest(request)
			.build();
		
		send(message);
	}
	
	
	public static int RANDOM_GUESS_WORD_INTERVAL = 5;
	public void setGuessWordTimer(){
				
		clearGuessWordTimer();
		
		guessWordTimer = new Timer();
		guessWordTimer.schedule(new TimerTask(){

			@Override
			public void run() {	
				try{
					String guessWord = null;
					
					if (guessCount >= 3){
						guessWord = (RandomUtil.random(1) == 0) ? word : "WRONG2";
					}
					else{
						guessWord = "WRONG1";
					}
					
					guessCount ++;
					sendGuessWord(guessWord);
					
				}
				catch (Exception e){
					GameLog.error(sessionId, e, "robot client guess word timer ");
				}

				// schedule next timer
				setGuessWordTimer();
			}
			
		}, 1000*RandomUtil.random(RANDOM_GUESS_WORD_INTERVAL));
	}
	
	public void clearGuessWordTimer(){
		if (guessWordTimer != null){
			guessWordTimer.cancel();
			guessWordTimer = null;
		}
		
	}

	public void resetPlayData() {
		clearGuessWordTimer();
		guessCount = 0;
	}
	
}
