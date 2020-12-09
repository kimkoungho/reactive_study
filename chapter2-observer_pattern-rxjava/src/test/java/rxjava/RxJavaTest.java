package rxjava;

import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RxJavaTest {

    @SuppressWarnings("Depricated")
    @Test
    public void testObservableAndSubscriber() {
        Observable<String> observable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {
                        sub.onNext("Hello, reactive world!");
                        sub.onCompleted();
                    }
                }
        );

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
            @Override
            public void onCompleted() {
                System.out.println("Done!");
            }
            @Override
            public void onError(Throwable e) {
                System.out.println(e);
            }
        };

        observable.subscribe(subscriber);
    }

    @SuppressWarnings("Depricated")
    @Test
    public void testObservableAndSubscriber_lambda() {
        Observable.create(
                sub -> {
                    sub.onNext("Hello, reactive world!");
                    sub.onCompleted();
                }
        ).subscribe(
                System.out::println, // onNext
                System.out::println, // onError
                () -> System.out.println("Done!") // onCompleted
        );
    }

    @Test
    public void testObservableCreate() {
        Observable.just("1", "2", "3", "4")
                .subscribe(
                        System.out::println, // onNext
                        System.out::println, // onError
                        () -> System.out.println("Done!") // onCompleted
                );
        Observable.from(new String[]{"A", "B", "C"})
                .subscribe(
                        System.out::println, // onNext
                        System.out::println, // onError
                        () -> System.out.println("Done!") // onCompleted
                );
        Observable.from(Collections.emptyList())
                .subscribe(
                    System.out::println, // onNext
                    System.out::println, // onError
                    () -> System.out.println("Done!") // onCompleted
                );
    }

    @Test
    public void testObservableCreateForCallableAndFuture() {
        Observable<String> hello = Observable.fromCallable(() -> "Hello ");
        Future<String> future = Executors.newCachedThreadPool().submit(() -> "World");
        Observable<String> world = Observable.from(future);

        Observable.concat(hello, world, Observable.just("!"))
                .forEach(System.out::print);
    }

    @Test
    public void testAsyncEvent() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(e -> System.out.println("Received: " + e));

        // 이 코드를 제거하면 아무것도 일어나지 않는다
        Thread.sleep(5000);
    }

    @Test
    public void testUnsubscribe() throws InterruptedException {
        // countDown() 3 번 호출되야 종료됨
        CountDownLatch externalSignal = new CountDownLatch(3);

        Subscription subscription = Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);

        // 3초간 대기하도록 ..
        Thread thread = new Thread(() -> {
            try {
                while(externalSignal.getCount() > 0) {
                    Thread.sleep(1000);
                    externalSignal.countDown();
                }
            } catch(InterruptedException e){}
        });
        thread.start();

        externalSignal.await();
        subscription.unsubscribe();

        // unsubscribe 이후에는 몇 초를 대기하던 출력되지 않음
        Thread.sleep(2000);
    }

    @Test
    public void testMap() throws InterruptedException {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(e -> e * 2)
                .subscribe(System.out::println);

        Thread.sleep(500);
    }

    @Test
    public void testCount() throws InterruptedException {
        Observable.just("1", "2", "3", "4")
                .count()
                .subscribe(System.out::println);

        // 무한 스트림은 결과가 없다
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .count()
                .subscribe(System.out::println);

        Thread.sleep(500);
    }

    @Test
    public void testZip() {
        Observable.zip(
                Observable.just("A", "B", "C"),
                Observable.just("1", "2", "3"),
                (x, y) -> x + y
        ).forEach(System.out::println);
    }

    @Test
    public void testTransformer() {
        Observable.Transformer<String, Integer> toLengthFun = (stringObservable) -> stringObservable.map(String::length);

        Observable.just("leo1", "leo@kakao.com", "abcdefg")
                .compose(toLengthFun)
                .subscribe(System.out::println);
    }

    @Test
    public void testSchedulers() throws InterruptedException {
        String query = "query";
        Observable.fromCallable(() -> doSlowSyncRequest(query))
                .subscribeOn(Schedulers.io())
                .subscribe(this::processResult);

        Thread.sleep(1000);
    }

    private String doSlowSyncRequest(String query) {
        return "result";
    }

    private void processResult(String result) {
        System.out.println(Thread.currentThread().getName() + ": " + result);
    }
}
