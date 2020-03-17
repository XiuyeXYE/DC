/**
 * http://usejsdoc.org/
 */

;
if (!xy) {
	throw "Need xy2.js framework!!!";
}


/**
 * 
 * Ajax API
 * 
 */

xy.D(function Ajax() {
	xy.notInstanceof(this, Ajax, 'Constructor Ajax requires "new"!');
	if (xy.fnExist(XMLHttpRequest)) {
		this.xhr = new XMLHttpRequest();
	}
}, {
	exist: function () {
		return xy.oExist(this.xhr);
	},
	q: function (params) {
		if (this.exist() && xy.oExist(params)) {

			var url = params.url;
			var data = params.data;
			var method = xy.dv(params.type, xy.Ajax.TYPE_GET);
			var success = params.success;
			var dataType = xy.dv(params.dataType, xy.Ajax.DATA_TYPE_DEFAULT);
			var headers = params.headers;
			var async = xy.dv(params.async, true);

			//set listener
			this.xhr.onerror = params.error;
			this.xhr.onloadstart = params.loadstart;
			this.xhr.onprogress = params.progress;
			this.xhr.onabort = params.abort;
			this.xhr.onload = params.load;
			this.xhr.ontimeout = params.timeout;
			this.xhr.onloadend = params.onloadend;

			this.xhr.open(method, url, async, params.user, params.password);

			if (xy.oExist(headers)) {
				for (var h in headers) {
					var v = headers[h];
					this.xhr.setRequestHeader(h, v);
				}
			}

			this.xhr.responseType = dataType;

			this.xhr.onreadystatechange = function (e) {
				if (params.readystatechange) {
					params.readystatechange(e);
				}
				var xhrt = e.target;
				if (xhrt.readyState == XMLHttpRequest.DONE) {
					if (xhrt.status == 200 && xy.fnExist(success)) {
						success(xhrt.response, xhrt);
					}
				}
			};
			if (params.withCredentials) {
				this.xhr.withCredentials = true;
			} else {
				this.xhr.withCredentials = false;
			}

			this.xhr.send(data);
		}

	}
}, {
	TYPE_GET: 'GET',
	TYPE_POST: 'POST',
	TYPE_PUT: 'PUT',
	TYPE_DELETE: 'DELETE',
	DATA_TYPE_DEFAULT: '',
	DATA_TYPE_JSON: 'json',
	DATA_TYPE_TEXT: 'text',
	DATA_TYPE_BLOB: 'blob',
	DATA_TYPE_DOM: 'document',
	DATA_TYPE_BUFFER: 'arraybuffer'
}, true);

xy.D(function ajax(params) {
	new this.Ajax().q(params);
});

xy.D(function getPublicNetworkIP(callback) {// kind C IP
	const ipAPI = 'http://api.ipify.org?format=json';
	// fetch(ipAPI)
	// .then(response => response.json())
	// .then(data => data.ip).then(callback);
	xy.ajax({
		url: ipAPI,
		success: callback
	});
});
