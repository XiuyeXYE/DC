package com.xy.dc;



import com.xiuye.util.log.XLog;

import io.reactivex.Flowable;

public class RxJavaTest {

	public static void main(String[] args) {
		
		Flowable.just("Hello world").subscribe(XLog::log);
	}
	
	
	
	
}
