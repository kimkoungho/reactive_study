package com.study.spring5reactive.chapter1reactive.strategy.futures;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.concurrent.Future;

public interface ShoppingCardServiceFutures {
    Future<Output> calculate(Input value);
}
