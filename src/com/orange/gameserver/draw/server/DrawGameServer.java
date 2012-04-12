package com.orange.gameserver.draw.server;
import java.net.InetSocketAddress;

import java.util.concurrent.Executors;  

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class DrawGameServer {

	
	private static final Logger logger = Logger.getLogger(DrawGameServer.class
			.getName());
	
	
	public static int getPort() {
		String port = System.getProperty("server.port");
		if (port != null && !port.isEmpty()){
			return Integer.parseInt(port);
		}
		return 8080; // default
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
		logger.info("Start traffic server at "+getPort());
	}

}
