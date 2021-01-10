package com.study.spring5reactive.chapter1reactive.strategy;

import com.study.spring5reactive.chapter1reactive.strategy.callbacks.AsyncShoppingCardServiceCallbacks;
import com.study.spring5reactive.chapter1reactive.strategy.callbacks.OrdersServiceCallbacks;
import com.study.spring5reactive.chapter1reactive.strategy.callbacks.SyncShoppingCardServiceCallbacks;
import com.study.spring5reactive.chapter1reactive.strategy.completion_stage.CompletionStageShoppingCardService;
import com.study.spring5reactive.chapter1reactive.strategy.completion_stage.OrdersServiceCompletion;
import com.study.spring5reactive.chapter1reactive.strategy.futures.FutureShoppingCardServiceFutures;
import com.study.spring5reactive.chapter1reactive.strategy.futures.OrdersServiceFutures;
import com.study.spring5reactive.chapter1reactive.strategy.imperative.BlockingShoppingCardServiceImperative;
import com.study.spring5reactive.chapter1reactive.strategy.imperative.OrdersServiceImperative;
import org.junit.jupiter.api.Test;



class OrdersServiceTest {

    @Test
    void process_imperative() {
        long start = System.currentTimeMillis();
        OrdersServiceImperative service1 = new OrdersServiceImperative(new BlockingShoppingCardServiceImperative());
        service1.process();

        OrdersServiceImperative service2 = new OrdersServiceImperative(new BlockingShoppingCardServiceImperative());
        service2.process();

        long end = System.currentTimeMillis();
        // 대략 2초
        System.out.println("Total time in millis : " + (end - start));
    }

    @Test
    void process_callbacks() throws InterruptedException {
        long start = System.currentTimeMillis();

        OrdersServiceCallbacks ordersServiceSync = new OrdersServiceCallbacks(new SyncShoppingCardServiceCallbacks());
        OrdersServiceCallbacks ordersServiceAsync = new OrdersServiceCallbacks(new AsyncShoppingCardServiceCallbacks());

        ordersServiceAsync.process();
        ordersServiceAsync.process();
        ordersServiceSync.process();

        long end = System.currentTimeMillis();

        // 4 ms (블로킹 걸리지 않음)
        System.out.println("Total time in millis : " + (end - start));
        Thread.sleep(1000);
    }

    @Test
    void process_future() {
        long start = System.currentTimeMillis();

        OrdersServiceFutures ordersServiceFutures = new OrdersServiceFutures(new FutureShoppingCardServiceFutures());

        ordersServiceFutures.process();
        ordersServiceFutures.process();

        long end = System.currentTimeMillis();
        // 2초 (process 내부에서 블로킹이 일어나기 때문)
        System.out.println("Total time in millis : " + (end - start));
    }

    @Test
    void process_completion_state() throws InterruptedException {
        long start = System.currentTimeMillis();

        OrdersServiceCompletion ordersServiceCompletion = new OrdersServiceCompletion(new CompletionStageShoppingCardService());

        ordersServiceCompletion.process();
        ordersServiceCompletion.process();

        long end = System.currentTimeMillis();
        // 7 (콜백 처리를 전달하는 방식으로 blocking 이 일어나지 않음)
        // ㄴ 아래 출력이 발생한 후에 연산이 수행됨
        System.out.println("Total time in millis : " + (end - start));

        Thread.sleep(1000);
    }
}