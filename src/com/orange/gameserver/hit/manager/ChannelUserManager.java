package com.orange.gameserver.hit.manager;

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
	
	Object transactionLock = new Object();
	
	// thread-safe singleton implementation
    private static ChannelUserManager manager = new ChannelUserManager();     
    private ChannelUserManager(){		
	} 	    
    public static ChannelUserManager getInstance() { 
    	return manager; 
    } 
    
    public void addChannel(Channel channel){
    	synchronized(transactionLock){
    		logger.info("Create Channel " + channel.toString());
    		CopyOnWriteArrayList<String> userList = new CopyOnWriteArrayList<String>();
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void addUserIntoChannel(Channel channel, String userId){
    	
    	synchronized(transactionLock){
    		logger.info("Add " + userId + " Into Channel " + channel.toString());
    		CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
    		if (userList == null)
    			return;
    	
    		userList.add(userId);
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void removeUserFromChannel(Channel channel, String userId){
    	synchronized(transactionLock){
    		logger.info("Remove " + userId + " From Channel " + channel.toString());    		
	    	CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
	    	if (userList == null)
	    		return;

    		userList.remove(userId);
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void removeChannel(Channel channel){
		logger.info("Remove Channel " + channel.toString());    		
    	channelUserMap.remove(channel);
    }
    
	public List<String> findUsersInChannel(Channel channel) {		
		synchronized(transactionLock){
			CopyOnWriteArrayList<String> list = channelUserMap.get(channel);
			if (list == null)
				return Collections.emptyList();
			
			List<String> userList = new ArrayList<String>();
			userList.addAll(list);
			return userList;
		}
	}
}
