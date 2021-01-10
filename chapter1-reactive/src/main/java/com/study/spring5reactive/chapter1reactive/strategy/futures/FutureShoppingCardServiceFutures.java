package com.study.spring5reactive.chapter1reactive.strategy.futures;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class FutureShoppingCardServiceFutures implements ShoppingCardServiceFutures {
    @Override
    public Future<Output> calculate(Input value) {
        FutureTask<Output> futureTask = new FutureTask<>(() ->{
            Thread.sleep(1000);
            return new Output();
        });

        // 별도의 스레드에서 해당 future 시작
        new Thread(futureTask).start();

        return futureTask;
    }
}
