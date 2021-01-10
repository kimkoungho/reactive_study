package com.study.spring5reactive.chapter1reactive.strategy.imperative;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

public class BlockingShoppingCardServiceImperative implements ShoppingCardServiceImperative {
    @Override
    public Output calculate(Input value) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Output();
    }
}
