package com.study.spring5reactive.rx_java;

import org.springframework.stereotype.Component;
import rx.Observable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class TemperatureSensor {
    private final Random rnd = new Random();

    // data stream 정의 (Observable stream)
    private final Observable<Temperature> dataStream = Observable
            .range(0, Integer.MAX_VALUE) // 스트림 생성
            // 변환 작업 Integer -> Temperature 결합
            // public final <R> Observable<R> concatMap(Func1<? super T, ? extends Observable<? extends R>> func)
            .concatMap(tick ->
                    Observable.just(tick) // Observable 스트림으로 변환
                    .delay(rnd.nextInt(5000), TimeUnit.MILLISECONDS) // delay
                    .map(tickValue -> this.probe()) // Temperature
            )
            // publish 로 대상 스트림으로 브로드 캐스팅
            // public final ConnectableObservable<T> publish()
            .publish()
            // 스트림에 하나 이상의 구독자가 있을 때에만 생성하도록
            // public Observable<T> refCount()
            .refCount();

    private Temperature probe() {
//        System.out.println("probe");
        return new Temperature(16 + rnd.nextGaussian() * 10);
    }

    public Observable<Temperature> temperatureStream() {
        return dataStream;
    }
}
