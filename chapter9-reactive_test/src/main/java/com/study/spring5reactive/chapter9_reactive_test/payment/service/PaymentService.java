package com.study.spring5reactive.chapter9_reactive_test.payment.service;

import com.study.spring5reactive.chapter9_reactive_test.payment.model.Payment;
import com.study.spring5reactive.chapter9_reactive_test.payment.repository.PaymentRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;


@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WebClient client;


    public PaymentService(PaymentRepository paymentRepository, WebClient.Builder builder) {
        this.paymentRepository = paymentRepository;
        this.client = builder.baseUrl("http://api.bank.com/submit")
                .build();
    }

    public Mono<String> send(Mono<Payment> payment) {
        return payment.zipWith(
                ReactiveSecurityContextHolder.getContext(),
                (p, c) -> p.withUser(c.getAuthentication().getName())
        )
                .flatMap(p -> client.post()
                        .syncBody(p)
                        .retrieve()
                        .bodyToMono(String.class)
                        .then(paymentRepository.save(p)))
                .map(Payment::getId);
    }


    public Flux<Payment> list() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .flatMapMany(paymentRepository::findAllByUser);
    }
}
