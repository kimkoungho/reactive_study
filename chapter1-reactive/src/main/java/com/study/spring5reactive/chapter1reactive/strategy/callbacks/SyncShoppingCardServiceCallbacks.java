package com.study.spring5reactive.chapter1reactive.strategy.callbacks;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.function.Consumer;

public class SyncShoppingCardServiceCallbacks implements ShoppingCardServiceCallbacks {
    @Override
    public void calculate(Input value, Consumer<Output> consumer) {
        // No blocking operation, better to immediately provide answer
        consumer.accept(new Output());
    }
}
