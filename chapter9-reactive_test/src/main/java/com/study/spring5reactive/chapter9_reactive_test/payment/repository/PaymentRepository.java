package com.study.spring5reactive.chapter9_reactive_test.payment.repository;

import com.study.spring5reactive.chapter9_reactive_test.payment.model.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {

    Flux<Payment> findAllByUser(String user);
}
