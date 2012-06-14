package com.orange.gameserver.draw.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.antlr.grammar.v3.ANTLRv3Parser.finallyClause_return;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.orange.gameserver.draw.dao.GameSession;
import com.orange.gameserver.draw.server.GameService;
import com.orange.network.game.protocol.constants.GameConstantsProtos.GameCommandType;

public class ChannelUserManager {

	protected static final Logger logger = Logger.getLogger("ChannelUserManager");
	ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(5);
	
	ConcurrentMap<Channel, CopyOnWriteArrayList<String>> channelUserMap 
		= new ConcurrentHashMap<Channel, CopyOnWriteArrayList<String>>();
	
	ConcurrentMap<Channel, ScheduledFuture> channelTimeOutFutureMap 
	= new ConcurrentHashMap<Channel, ScheduledFuture>();
	
	// thread-safe singleton implementation
    private static ChannelUserManager manager = new ChannelUserManager();     
    private ChannelUserManager(){		
	} 	    
    public static ChannelUserManager getInstance() { 
    	return manager; 
    } 
    
    public void addChannel(Channel channel){
		logger.info("<addChannel> Channel " + channel.toString());
		logger.info("<addChannel> Channel Count = " + channelUserMap.size());
		CopyOnWriteArrayList<String> userList = new CopyOnWriteArrayList<String>();
		channelUserMap.putIfAbsent(channel, userList);
    }
    
    public void addUserIntoChannel(Channel channel, String userId){    	
		logger.info("<addUserIntoChannel> Add " + userId + " Into Channel " + channel.toString());
		CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
		if (userList == null)
			return;
	
		userList.add(userId);
    }
    
    public void removeUserFromChannel(Channel channel, String userId){
		logger.info("<removeUserFromChannel> Remove " + userId + " From Channel " + channel.toString());    		
    	CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
    	if (userList == null)
    		return;

		userList.remove(userId);
    }
    
    public void removeChannel(Channel channel){
		logger.info("<removeChannel> Channel " + channel.toString() + ", before remove count = " + channelUserMap.size());
		clearChannelTimeOut(channel);
    	channelUserMap.remove(channel);    	
    	channelTimeOutFutureMap.remove(channel);
    	
    	try{
	    	if (channel.isConnected()){
	    		channel.disconnect();
	    	}
    	}
    	catch (Exception e){    	
    		logger.error("<removeChannel> catch exception = "+e.toString(), e);
    	}
    	finally{
        	channel.close();    		
    	}
		
    }
    
	public List<String> findUsersInChannel(Channel channel) {		
		CopyOnWriteArrayList<String> list = channelUserMap.get(channel);
		if (list == null)
			return Collections.emptyList();
		
		List<String> userList = new ArrayList<String>();
		userList.addAll(list);
		return userList;
	}
	
	static final int DEFAULT_USER_TIMEOUT_SECONDS = 60*5;
	
	/*
	public void resetUserTimeOut(final Channel channel) {
		
		clearChannelTimeOut(channel);
		
		Callable callable = new Callable(){
			@Override
			public Object call()  {
				logger.info("Channel User Timer Out Event Fired! Channel="+channel.toString());
				processDisconnectChannel(channel);
				return null;
			}			
		};
		
		ScheduledFuture newFuture = scheduleService.schedule(callable, 
				DEFAULT_USER_TIMEOUT_SECONDS, TimeUnit.SECONDS);  			
		
		channelTimeOutFutureMap.put(channel, newFuture);
	}
	*/
	
	public void clearChannelTimeOut(Channel channel) {
		if (channel == null)
			return;
		
		ScheduledFuture future = channelTimeOutFutureMap.get(channel);
		if (future != null){
			future.cancel(false);
			future = null;
		}
	}

	public void processDisconnectChannel(Channel channel){
		List<String> userIdList = findUsersInChannel(channel);
		for (String userId : userIdList){
			int sessionId = UserManager.getInstance().findGameSessionIdByUserId(userId);
			if (sessionId != -1){
				GameSession session = GameSessionManager.getInstance().findGameSessionById(sessionId);
				GameSessionManager.getInstance().userQuitSession(userId, session, true);
			}

			UserManager.getInstance().removeOnlineUserById(userId);
		}
		
		// remove channel
		ChannelUserManager.getInstance().removeChannel(channel);		
	}
}
