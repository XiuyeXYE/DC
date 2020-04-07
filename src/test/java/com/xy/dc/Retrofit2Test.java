package com.xy.dc;

import org.junit.jupiter.api.Test;

import com.xiuye.util.log.XLog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;

public class Retrofit2Test {

	@Test
	public void test() {
		Observable<String> observable = new Observable<String>() {

			@Override
			protected void subscribeActual(Observer<? super String> observer) {
				observer.onNext("Doing something!");
				observer.onNext("Doing something!");
				observer.onNext("Doing something!");
			}
		};

		observable.subscribeOn(Schedulers.io())
		.map(d -> d + "map1")
		.map(d -> d + "map2")
		.map(d -> d + "map3")
		.observeOn(Schedulers.computation())
		.subscribe(s -> XLog.log(s));

		
		
	}

}
