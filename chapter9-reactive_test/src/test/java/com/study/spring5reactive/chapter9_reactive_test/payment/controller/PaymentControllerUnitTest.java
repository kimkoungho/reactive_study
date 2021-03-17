package com.study.spring5reactive.chapter9_reactive_test.payment.controller;

import com.study.spring5reactive.chapter9_reactive_test.payment.model.Payment;
import com.study.spring5reactive.chapter9_reactive_test.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class PaymentControllerUnitTest {

    @Test
    public void verifyRespondWithExpectedPayment() {
        PaymentService paymentService = Mockito.mock(PaymentService.class);
        PaymentController paymentController = new PaymentController(paymentService);

        Payment[] payments = new Payment[]{new Payment("1", "a"), new Payment("2", "b"), new Payment("3", "c")};
        BDDMockito.given(paymentService.list())
                .willReturn(Flux.just(payments));

        WebTestClient
                .bindToController(paymentController)
                .build()
                .get()// HTTP method
                .uri("/payments")
                .exchange()// API call
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().is2xxSuccessful()
                .returnResult(Payment.class) // result class
                .getResponseBody() // Flux<Payment> 반환
                .as(StepVerifier::create) // StepVerifier 를 이용한 검증 시작
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }
}