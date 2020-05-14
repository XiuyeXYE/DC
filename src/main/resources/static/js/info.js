
xy.ready(()=>{
	
	let info = xy.byId("info");
	xdebug(info);
	let h1 = xy.createDom("h1");
	xdebug(h1);
	h1.text(JSON.stringify(navigator.userAgent));
	info.appendChild(h1);
	
});
