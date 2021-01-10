package com.study.spring5reactive.chapter1reactive.strategy.callbacks;

import com.study.spring5reactive.chapter1reactive.common.Input;

public class OrdersServiceCallbacks {
    private final ShoppingCardServiceCallbacks shoppingCardServiceCallbacks;

    public OrdersServiceCallbacks(ShoppingCardServiceCallbacks shoppingCardServiceCallbacks) {
        this.shoppingCardServiceCallbacks = shoppingCardServiceCallbacks;
    }

    public void process() {
        Input input = new Input();
        shoppingCardServiceCallbacks.calculate(input, output -> {
            System.out.println(shoppingCardServiceCallbacks.getClass().getSimpleName() + " execution completed");
        });
    }
}
