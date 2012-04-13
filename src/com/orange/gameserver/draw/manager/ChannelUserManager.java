package com.orange.gameserver.draw.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;


public class ChannelUserManager {

	protected static final Logger logger = Logger.getLogger("ChannelUserManager");
	
	ConcurrentMap<Channel, CopyOnWriteArrayList<String>> channelUserMap 
		= new ConcurrentHashMap<Channel, CopyOnWriteArrayList<String>>();
	
	// thread-safe singleton implementation
    private static ChannelUserManager manager = new ChannelUserManager();     
    private ChannelUserManager(){		
	} 	    
    public static ChannelUserManager getInstance() { 
    	return manager; 
    } 
    
    public void addChannel(Channel channel){
		logger.info("<addChannel> Channel " + channel.toString());
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
		logger.info("<removeChannel> Channel " + channel.toString());    		
    	channelUserMap.remove(channel);
    	if (channel.isOpen()){
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
}
