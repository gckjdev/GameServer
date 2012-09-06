package com.orange.gameserver.draw.server;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;  

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.orange.game.traffic.server.ServerMonitor;

public class DrawGameServer {

	
	private static final Logger logger = Logger.getLogger(DrawGameServer.class
			.getName());
	
	public static final int LANGUAGE_CHINESE = 1;
	public static final int LANGUAGE_ENGLISH = 2;
	
	public static int getPort() {
		String port = System.getProperty("server.port");
		if (port != null && !port.isEmpty()){
			return Integer.parseInt(port);
		}
		return 8080; // default
	}
	
	public static int getLanguage() {
		String lang = System.getProperty("config.lang");
		if (lang != null && !lang.isEmpty()){
			return Integer.parseInt(lang);
		}
		return LANGUAGE_CHINESE; // default
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GameService.getInstance().createWorkerThreads(25);
		
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()				
				));
		
		bootstrap.setPipelineFactory(new GameServerPipelineFactory());
		
		bootstrap.bind(new InetSocketAddress(getPort()));
		logger.info("Start Traffic Server At Port "+getPort());
		
		// This code is to initiate the listener.
		ServerMonitor.getInstance().start();
    }

	}

