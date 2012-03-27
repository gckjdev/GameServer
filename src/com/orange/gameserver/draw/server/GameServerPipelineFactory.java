package com.orange.gameserver.draw.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
//import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
//import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
//import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.orange.network.game.protocol.message.GameMessageProtos;;

public class GameServerPipelineFactory implements ChannelPipelineFactory {

	public ChannelPipeline getPipeline() throws Exception {
	
		ChannelPipeline p = Channels.pipeline();
		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		p.addLast("protobufDecoder", new ProtobufDecoder(GameMessageProtos.GameMessage.getDefaultInstance()));
		 
		p.addLast("frameEncoder", new LengthFieldPrepender(4));
		p.addLast("protobufEncoder", new ProtobufEncoder());
		 
		p.addLast("handler", new GameServerHandler());
		return p;	
	}

}
