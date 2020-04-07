package com.xy.dc;




import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.xiuye.util.log.XLog;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RxJavaTest {

	public static void main(String[] args) {
		
		Flowable.just("Hello world").subscribe(XLog::log);
	}
	
	@Test
	public void testRXJava() {
		Flowable.just("KKKKKK").subscribe(XLog::println);
		Observable.create(new ObservableOnSubscribe<String>() {

			@Override
			public void subscribe(ObservableEmitter<String> emitter) throws Exception {
				emitter.onNext("A");
				emitter.onNext("B");
				emitter.onNext("C");
				emitter.onComplete();
			}
			
		}).subscribe(new Observer<String>() {

			@Override
			public void onSubscribe(Disposable d) {
//				d.dispose();
				XLog.log(d.isDisposed());
			}

			@Override
			public void onNext(String t) {
				XLog.log(t);
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete() {
				XLog.log("complete");
			}
			
		});
		
		
	}
	
	@Test
	public void testRXjavaAPI1() {
		Observable.create(emitter -> {
		     while (!emitter.isDisposed()) {
		         long time = System.currentTimeMillis();
		         emitter.onNext(time);
		         if (time % 2 != 0) {
		             emitter.onError(new IllegalStateException("Odd millisecond!"));
		             break;
		         }
		     }
		})
		.subscribe(System.out::println, Throwable::printStackTrace);
	}
	
	@Test
	public void testRXjavaAPI2() {
		Flowable.range(1, 10)
		.observeOn(Schedulers.computation())
		.map(v->v*v)
		.blockingSubscribe(d->XLog.log(d));
		
		Flowable.range(1,10)
		.parallel()
		.runOn(Schedulers.computation())
		.map(v->v*v)
		.sequential()
		.blockingSubscribe(XLog::println);
		
		XLog.log("====================");
		AtomicInteger count = new AtomicInteger();

		Observable.range(1, 10)
		  .doOnNext(ignored -> count.incrementAndGet())
		  .ignoreElements()
		  .andThen(Single.just(count.get()))
		  .subscribe(System.out::println);
		
	}
	
	
	
	
	
}
