package com.orange.gameserver.hit.server;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.example.tutorial.AddressBookProtos;
import com.orange.network.game.protocol.GameProtos;



public class GameServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(GameServerHandler.class
			.getName());

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		logger.info(e.toString());
		if (e instanceof ChannelStateEvent) {
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		
//		AddressBookProtos.AddressBook addr = (AddressBookProtos.AddressBook)e.getMessage();
//		logger.info("addr = " + addr.toString());
		
//		AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder().
//			setId(2234).setName("Tina").build();
//		
//		AddressBookProtos.AddressBook address = AddressBookProtos.AddressBook.newBuilder().addPerson(person).build();
//		e.getChannel().write(address);		
		
		GameProtos.GameRequest request = (GameProtos.GameRequest)e.getMessage();
		Date now = new Date();
		String gameId = UUID.randomUUID().toString();
		String gameName = request.getNewGameCommand().getName();
		String userId = request.getNewGameCommand().getUserId();
		int requestId = request.getId();

		logger.info(String.format("[%08X] [RECV] %s", requestId, e.getMessage().toString()));
		if (request.getCommand() == GameProtos.GameRequest.CommandType.NEW_GAME){
			GameProtos.Game newGame = GameProtos.Game.newBuilder()
													.setCreateBy(userId)
													.setCreateTime((int)now.getTime())
													.setGameId(gameId)
													.setName(gameName)
													.setHost(userId)
													.addUsers(userId)
													.build();
			
			GameProtos.NewGameResponse newGameResponse = GameProtos.NewGameResponse.newBuilder()
													.setGame(newGame)
													.build();
			
			GameProtos.GameResponse response = GameProtos.GameResponse.newBuilder().
						setId(request.getId()).
						setResultCode(GameProtos.GameResponse.ResultCodeType.SUCCESS).						
						setNewGameResp(newGameResponse).
						build();

			logger.info(String.format("[%08X] [SEND] %s", requestId, response.toString()));
			e.getChannel().write(response);
		}
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("TestHandler catch unexpected exception .", e.getCause());
		e.getChannel().close();
	}
			
//	private static String toString(Continent c) {
//		return "" + c.name().charAt(0) + c.name().toLowerCase().substring(1);
//	}
}
