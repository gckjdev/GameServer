package com.orange.gameserver.hit.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jboss.netty.channel.Channel;


public class ChannelUserManager {

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
    		CopyOnWriteArrayList<String> userList = new CopyOnWriteArrayList<String>();
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void addUserIntoChannel(Channel channel, String userId){
    	
    	synchronized(transactionLock){
    		CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
    		if (userList == null)
    			return;
    	
    		userList.add(userId);
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void removeUserFromChannel(Channel channel, String userId){
    	synchronized(transactionLock){
	    	CopyOnWriteArrayList<String> userList = channelUserMap.get(channel);
	    	if (userList == null)
	    		return;

    		userList.remove(userId);
    		channelUserMap.put(channel, userList);
    	}
    }
    
    public void removeChannel(Channel channel){
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
