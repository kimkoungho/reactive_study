package com.study.spring5reactive.chapter1reactive.strategy.completion_stage;

import com.study.spring5reactive.chapter1reactive.common.Input;

public class OrdersServiceCompletion {
    private final ShoppingCardServiceCompletion shoppingCardService;

    public OrdersServiceCompletion(ShoppingCardServiceCompletion shoppingCardService) {
        this.shoppingCardService = shoppingCardService;
    }

    public void process() {
        Input input = new Input();

        shoppingCardService.calculate(input)
                .thenAccept(v -> System.out.println(shoppingCardService.getClass().getSimpleName() + " execution completed"));

        System.out.println(shoppingCardService.getClass().getSimpleName() + " calculate called");
    }
}
