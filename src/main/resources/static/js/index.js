/**
 * http://usejsdoc.org/
 */

//xy.DEBUG_TIME = false;

runtime(xy.ready,xy,
/*xy("ready",*/()=>{
	xdebug("websocket demo");
	
	let ws = new WebSocket("ws://localhost:8080/im/user");
	xdebug(ws);
	
	xy.extend(ws,{
		onopen(){
			xdebug("websocket opened");
			ws.send("websocket is opened in the front!")
		},
		onclose(){
			xdebug("websocket close");
		},
		onmessage(msg){
			xdebug("websocket receive msg:",msg);
		},
		onerror(){
			xdebug("websocket errors");
		}
	});
	
});


