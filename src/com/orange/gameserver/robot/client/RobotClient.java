package com.orange.gameserver.robot.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.orange.gameclient.draw.test.dao.ClientUser;
import com.orange.gameserver.draw.dao.User;
import com.orange.gameserver.draw.server.DrawGameServer;
import com.orange.gameserver.draw.utils.GameLog;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;
import com.orange.network.game.protocol.message.GameMessageProtos.GameMessage;
import com.orange.network.game.protocol.message.GameMessageProtos.JoinGameRequest;

public class RobotClient {

	final int sessionId;
	final String userId;
	final String nickName;
	final boolean gender;
	final String userAvatar;
	final int robotIndex;
	ChannelFuture future;
	ClientBootstrap bootstrap;
	
	AtomicInteger messageId = new AtomicInteger(1);
	Channel channel;
	ConcurrentHashMap<String, User> userList = new ConcurrentHashMap<String, User>();
	
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
        
        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();		
        
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
		
		if (channel != null && channel.isWritable()){
			GameLog.info(sessionId, "robot "+nickName+ " send JOIN GAME request");
			channel.write(message);
		}

	}

	public void disconnect() {
		GameLog.info(sessionId, "Robot " + nickName + " Disconnect");
		if (channel != null && channel.isConnected()){
			channel.disconnect();
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

	public void stop() {
		if (future != null){		
			future.cancel();
		}
		
        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();			        
	}
	
	
}
