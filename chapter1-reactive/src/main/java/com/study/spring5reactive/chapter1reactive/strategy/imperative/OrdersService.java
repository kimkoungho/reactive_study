package com.study.spring5reactive.chapter1reactive.strategy.imperative;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

public class OrdersService {
    private final ShoppingCardService scService;

    public OrdersService(ShoppingCardService scService) {
        this.scService = scService;
    }

    void process() {
        Input input = new Input();
        Output output = scService.calculate(input);

        System.out.println(scService.getClass().getSimpleName() + " execution completed");
    }
}
