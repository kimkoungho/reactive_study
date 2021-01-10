package com.study.spring5reactive.chapter1reactive.strategy.completion_stage;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompletionStageShoppingCardService implements ShoppingCardServiceCompletion {
    @Override
    public CompletionStage<Output> calculate(Input value) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           return new Output();
        });
    }
}
