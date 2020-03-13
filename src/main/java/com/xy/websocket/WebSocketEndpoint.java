package com.xy.websocket;

import java.io.IOException;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import com.xiuye.util.log.LogUtil;


//noly use for tomcat!
@ServerEndpoint("im/user")//not available
public class WebSocketEndpoint {

	private Session session;
	
	@OnOpen
	public void open(Session session,EndpointConfig config) {
		this.session = session;
		LogUtil.log("websocket open!");
	}
	@OnClose
	public void close() {
		LogUtil.log("websocket close!");
	}
	
	@OnError
	public void error(Session session, Throwable err) {
		LogUtil.log("websocket error");
		err.printStackTrace();
	}
	
	@OnMessage
	public void message(String msg) throws IOException {
		LogUtil.log("websocket message!",msg);
		session.getBasicRemote().sendText("Hello,client.I received your msg!");
	}
	
	
}
