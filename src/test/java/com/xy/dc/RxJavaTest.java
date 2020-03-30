package com.xy.dc;

import org.junit.Test;

import io.reactivex.Flowable;

public class RxJavaTest {

	@Test
	public void test() {
		 Flowable.just("Hello world").subscribe(System.out::println);
	}
	
}
