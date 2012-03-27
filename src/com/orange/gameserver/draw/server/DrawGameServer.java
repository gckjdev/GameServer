package com.orange.gameserver.draw.server;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;  

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class DrawGameServer {

	
	private static final Logger logger = Logger.getLogger(DrawGameServer.class
			.getName());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GameService.getInstance().createWorkerThreads(20);
		
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()				
				));
		
		bootstrap.setPipelineFactory(new GameServerPipelineFactory());
		
		bootstrap.bind(new InetSocketAddress(8080));
		logger.info("Start server at 8080");
	}

}
