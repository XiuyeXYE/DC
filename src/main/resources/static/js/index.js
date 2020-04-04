/**
 * http://usejsdoc.org/
 */

// xy.DEBUG_TIME = false;



runtime(xy.ready, xy,
/* xy("ready", */() => {
		xdebug("websocket demo");

		let wss = new xy.Socket("wss://" + document.location.host + "/im/user");
		// let wss = new WebSocket("wss://" + document.location.host + "/im/user");
		xdebug(wss);
		let i = 0;
		xy.copy(wss, {
			onopen() {
				xdebug("websocket opened");
				wss.send("websocket is opened in the front!")
			},
			onclose() {
				xdebug("websocket close");
			},
			onmessage(msg) {
				xdebug("websocket receive msg:", msg);
				xdebug("websocket receive msg data:", msg.data);
				//			xdebug(new Error().stack);
				wss.close();
				// ws.send(i++%1000);//repeat forever
			},
			onerror(e) {
				xdebug("websocket errors", e);
			}
		});
		// xy.ws = ws;

		new Promise((resolve, reject) => {
			let socket = new xy.Socket("wss://" + document.location.host + "/im/user");
			socket.onopen = (e) => {
				socket.send("Hello,Sever.Websocket opened!");
			};
			socket.onmessage = (e) => {
				resolve(e);
			};
		}).then(d =>
			d.data
		).then(d => {
			xdebug('client received :', d);
		});

		navigator.mediaDevices.getUserMedia({
			audio: true,
			video: true,
		}).then(
			(stream) => {

				xdebug("open stream");
				// let ctx = new AudioContext();
				// ctx.createMediaStreamSource(stream).connect(ctx.destination);
				xdebug("connected");
				// xdebug("dest:",ctx.destination);
				xdebug(stream);
				xdebug("audio tracks=", stream.getAudioTracks());
				xdebug("video tracks=", stream.getVideoTracks());
				xdebug("tracks=", stream.getTracks());
				// let video = xy.byId('mp4').get();
				// video.srcObject = stream;
				// video.onloadedmetadata = function(e) {
				// video.play();
				// };

				xy.byId('mp4').kv('srcObject', stream).kv('onloadedmetadata', (e) => {
					e.target.play();
				});
			}
			// ,
			// (err)=>{
			// xdebug(err);
			// }
		).catch((err) => {
			xdebug(err, err.name, err.message);
			Swal.fire(err.name, err.message, 'error');
		});



		let connection = new RTCPeerConnection({
			iceServers: [
				{
					urls: "stun:stun.stunprotocol.org"
				}
			]
		});

		



	});




