package com.xy.controller;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;


import com.xiuye.util.log.LogUtil;

@ServerEndpoint("im/user")
public class WebSocketController {

	@OnOpen
	public void open() {
		LogUtil.log("websocket open!");
	}
	@OnClose
	public void close() {
		LogUtil.log("websocket close!");
	}
	@OnMessage
	public void message(String msg) {
		LogUtil.log("websocket message!",msg);
	}
	
	
}
