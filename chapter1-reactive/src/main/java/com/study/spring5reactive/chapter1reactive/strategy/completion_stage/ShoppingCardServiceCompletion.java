package com.study.spring5reactive.chapter1reactive.strategy.completion_stage;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.concurrent.CompletionStage;

public interface ShoppingCardServiceCompletion {
    CompletionStage<Output> calculate(Input value);
}
