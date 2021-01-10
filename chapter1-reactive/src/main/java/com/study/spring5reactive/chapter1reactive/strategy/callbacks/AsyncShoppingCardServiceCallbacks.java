package com.study.spring5reactive.chapter1reactive.strategy.callbacks;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.function.Consumer;

public class AsyncShoppingCardServiceCallbacks implements ShoppingCardServiceCallbacks {
    @Override
    public void calculate(Input value, Consumer<Output> consumer) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            consumer.accept(new Output());
        }).start();
    }
}
