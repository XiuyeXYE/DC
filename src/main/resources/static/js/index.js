/**
 * http://usejsdoc.org/
 */

// xy.DEBUG_TIME = false;



runtime(xy.ready,xy,
/* xy("ready", */()=>{
	xdebug("websocket demo");
	
	let ws = new WebSocket("ws://"+document.location.host+"/im/user");
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
			ws.close();
// ws.send(i++%1000);//repeat forever
		},
		onerror(e){
			xdebug("websocket errors",e);
		}
	});
// xy.ws = ws;
	
	navigator.mediaDevices.getUserMedia({
		audio:false,
		video:{
			facingMode: 'user'
		},
	}).then(
	(stream)=>{
		
		xdebug("open stream");
// let ctx = new AudioContext();
// ctx.createMediaStreamSource(stream).connect(ctx.destination);
		xdebug("connected");
// xdebug("dest:",ctx.destination);
		xdebug(stream);
		xdebug("audio tracks=",stream.getAudioTracks());
		xdebug("video tracks=",stream.getVideoTracks());
		xdebug("tracks=",stream.getTracks());
// let video = xy.byId('mp4').get();
// video.srcObject = stream;
// video.onloadedmetadata = function(e) {
// video.play();
// };
		
		xy.byId('mp4').kv('srcObject',stream).kv('onloadedmetadata',(e)=>{
			e.target.play();
		});
	}
// ,
// (err)=>{
// xdebug(err);
// }
	).catch((err)=>{
		xdebug(err,err.name,err.message);
		alert(err.message);
	});
	
});




