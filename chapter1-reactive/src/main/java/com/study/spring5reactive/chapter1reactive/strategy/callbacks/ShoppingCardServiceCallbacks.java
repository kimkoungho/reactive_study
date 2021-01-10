package com.study.spring5reactive.chapter1reactive.strategy.callbacks;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

import java.util.function.Consumer;

public interface ShoppingCardServiceCallbacks {
    void calculate(Input value, Consumer<Output> consumer);
}
