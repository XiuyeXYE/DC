/**
 * http://usejsdoc.org/
 */

// xy.DEBUG_TIME = false;

runtime(xy.ready,xy,
/* xy("ready", */()=>{
	xdebug("websocket demo");
	
	let ws = new WebSocket("ws://localhost:8080/im/user");
	xdebug(ws);
	let i = 0;
	xy.copy(ws,{
		onopen(){
			xdebug("websocket opened");
			ws.send("websocket is opened in the front!")
		},
		onclose(){
			xdebug("websocket close");
		},
		onmessage(msg){
			xdebug("websocket receive msg:",msg);
//			ws.close();
//			ws.send(i++%1000);//repeat forever
		},
		onerror(e){
			xdebug("websocket errors",e);
		}
	});
//	xy.ws = ws;
	
	
	
});


