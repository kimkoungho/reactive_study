package com.study.spring5reactive.chapter9_reactive_test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeFunction;

@TestConfiguration
public class TestWebClientBuilderConfiguration {

    @Bean
    public WebClientCustomizer testWebClientCustomizer(ExchangeFunction exchangeFunction) {
        return builder -> builder.exchangeFunction(exchangeFunction);
    }
}
