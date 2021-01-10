package com.study.spring5reactive.chapter1reactive.strategy.imperative;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

public class OrdersServiceImperative {
    private final ShoppingCardServiceImperative scService;

    public OrdersServiceImperative(ShoppingCardServiceImperative scService) {
        this.scService = scService;
    }

    public void process() {
        Input input = new Input();
        Output output = scService.calculate(input);

        System.out.println(scService.getClass().getSimpleName() + " execution completed");
    }
}
