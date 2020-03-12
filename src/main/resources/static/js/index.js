/**
 * http://usejsdoc.org/
 */

xy("ready",()=>{
	xdebug("websocket demo");
	
	var ws = new WebSocket("ws://localhost:6653/im/user");
	xdebug(ws);
	
	xy.extend(ws,{
		onopen(){
			xdebug("websocket opened");
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


