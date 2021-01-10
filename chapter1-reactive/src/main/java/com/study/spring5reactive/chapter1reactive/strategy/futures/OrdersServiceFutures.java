package com.study.spring5reactive.chapter1reactive.strategy.futures;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OrdersServiceFutures {
    private final ShoppingCardServiceFutures shoppingCardService;

    public OrdersServiceFutures(ShoppingCardServiceFutures shoppingCardService) {
        this.shoppingCardService = shoppingCardService;
    }

    public void process() {
        Input input = new Input();
        // 여기서 비동기 호출이 발생
        Future<Output> result = shoppingCardService.calculate(input);

        System.out.println(shoppingCardService.getClass().getSimpleName() + " execution completed");

        try {
            // blocking
            result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
