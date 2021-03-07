package com.study.spring5reactive.chapter9_reactive_test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsEqual.equalTo;

public class StepVerifierExampleTest {

    @Test
    public void basic_test() {
        StepVerifier
                // publisher 연결
                .create(Flux.just("foo", "bar"))
                // 최초 이벤트는 구독과 관련된 이벤트
                .expectSubscription()
                // stream items
                .expectNext("foo")
                .expectNext("bar")
                // stream 종료 시그널 검증
                .expectComplete()
                // 실제 구독을 시작 - blocking 메소드
                .verify();
    }

    @Test
    public void expectNext_test() {
        // 0 ~ 99 까지의 원소 생성
        StepVerifier.create(Flux.range(0, 100))
                .expectSubscription()
                .expectNext(0)
                // 현재 스트림에서 몇개가 올지 검증
                .expectNextCount(98)
                .expectNext(99)
                .expectComplete()
                .verify();
    }

    public class Wallet {
        private String owner;
        public Wallet(String owner) { this.owner = owner; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
    }

    @Test
    public void filter_test() {
        // filter 가 적용된 publisher
        Publisher<Wallet> walletPublisher = Flux.just(new Wallet("admin"), new Wallet("user"), new Wallet("manager"))
                .filter(wallet -> wallet.getOwner().equals("admin"));

        StepVerifier.create(walletPublisher)
                .expectSubscription()
                //
                .recordWith(ArrayList::new)
                .expectNextCount(1)
                .consumeRecordedWith(wallets ->
                        assertThat(wallets, everyItem(hasProperty("owner", equalTo("admin")))))
                .expectComplete()
                .verify();
    }
}
