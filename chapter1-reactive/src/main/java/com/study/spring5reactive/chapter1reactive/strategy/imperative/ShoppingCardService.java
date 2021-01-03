package com.study.spring5reactive.chapter1reactive.strategy.imperative;

import com.study.spring5reactive.chapter1reactive.common.Input;
import com.study.spring5reactive.chapter1reactive.common.Output;

public interface ShoppingCardService {
    Output calculate(Input value);
}
