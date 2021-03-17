package com.study.spring5reactive.chapter9_reactive_test.payment.controller;

import com.study.spring5reactive.chapter9_reactive_test.MockClientResponse;
import com.study.spring5reactive.chapter9_reactive_test.TestSecurityConfiguration;
import com.study.spring5reactive.chapter9_reactive_test.TestWebClientBuilderConfiguration;
import com.study.spring5reactive.chapter9_reactive_test.payment.model.Payment;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ImportAutoConfiguration({
        TestSecurityConfiguration.class,
        TestWebClientBuilderConfiguration.class
})
@RunWith(SpringRunner.class)
@WithMockUser
@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    ExchangeFunction exchangeFunction;

    @Test
    public void verifyPaymentsWasSentAndStored() {
        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(MockClientResponse.create(201, Mono.empty())));

        client.post()
                .uri("/payments/")
                .syncBody(new Payment())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .expectComplete()
                .verify();

        Mockito.verify(exchangeFunction).exchange(Mockito.any());
    }

}