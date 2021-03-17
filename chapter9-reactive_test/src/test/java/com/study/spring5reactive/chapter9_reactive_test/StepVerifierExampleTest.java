package com.study.spring5reactive.chapter9_reactive_test;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.time.Duration;
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
        private String id;
        public Wallet(String owner) { this.owner = owner; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @Test
    public void filter_test() {
        // filter 가 적용된 publisher
        Publisher<Wallet> walletPublisher = Flux.just(new Wallet("admin"), new Wallet("user"), new Wallet("manager"))
                .filter(wallet -> wallet.getOwner().equals("admin"));

        StepVerifier.create(walletPublisher)
                .expectSubscription()
                // consumeRecordedWith 을 사용하기 위해서는 recordWith 이 선행되어야 함
                .recordWith(ArrayList::new)
                .expectNextCount(1)
                // publisher 보낸 모든 원소에 대해서 검증을 수행
                .consumeRecordedWith(wallets ->
                        // hamcrest 를 이용한 검증
                        assertThat(wallets, everyItem(hasProperty("owner", equalTo("admin")))))
                .expectComplete()
                .verify();
    }

    @Test
    public void expectNextMatches_test() {
        StepVerifier.create(Flux.just("alpha-foo", "betta-bar"))
                .expectSubscription()
                .expectNextMatches(e -> e.startsWith("alpha"))
                .expectNextMatches(e -> e.startsWith("betaa"))
                .expectComplete()
                .verify();
    }

    @Test
    public void assertNext_test() {
        Publisher<Wallet> walletPublisher = Flux.just(new Wallet("admin"), new Wallet("user"), new Wallet("manager"))
                .filter(wallet -> wallet.getOwner().equals("admin"));

        StepVerifier.create(walletPublisher)
                .expectSubscription()
                .assertNext(wallet -> assertThat(
                        wallet,
                        hasProperty("owner", equalTo("admin"))
                )).expectComplete()
                .verify();
    }

    @Test
    public void assertError_test() {
        // securityService.login
        StepVerifier.create(Flux.error(new BadCredentialsException("error")))
                .expectSubscription()
                .expectError(BadCredentialsException.class)
                .verify();
    }


    @Test
    public void thenCancel_test() {
        StepVerifier.create(Flux.interval(Duration.ofSeconds(5)))
                .expectSubscription()
                .expectNext(0L)
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    @Test
    public void backpressure_test() {
        Flux<String> websocketPublisher = Flux.just("Connected", "Price: $12.00");
        Flux<Object> error = Flux.error(Exceptions.failWithOverflow());
        Flux<Object> publisher = Flux.merge(websocketPublisher, error);

        // onBackpressureBuffer 로 다운 스트림을 보호할 수 있음
        // n = 0 : 초기 구독자가 요청하는 item 개수
        StepVerifier.create(publisher.onBackpressureBuffer(5), 0)
                .expectSubscription()
                .thenRequest(1)
                .expectNext("Connected")
                .thenRequest(1)
                .expectNext("Price: $12.00")
                .expectError(Exceptions.failWithOverflow().getClass())
                .verify();
    }

    @Test
    public void testPublisher_test() {
        Wallet wallet = new Wallet("admin");
        wallet.setId("1");
        Publisher<Wallet> walletPublisher = Flux.just(wallet);
        TestPublisher<String> idsPublisher = TestPublisher.create();

        StepVerifier.create(walletPublisher)
                .expectSubscription()
                .then(() -> idsPublisher.next("1"))
                .assertNext(w -> assertThat(w, hasProperty("id", equalTo("1"))))
                .then(idsPublisher::complete)
                .expectComplete()
                .verify();
    }

    public Flux<String> sendWithInterval() {
        return Flux.interval(Duration.ofMinutes(1))
                .zipWith(Flux.just("a", "b", "c"))
                .map(Tuple2::getT2);
    }

    @Ignore
    @Test
    public void sendWithInterval_test() {
        StepVerifier.create(sendWithInterval())
                .expectSubscription()
                .expectNext("a", "b", "c")
                .expectComplete()
                .verify();
    }

    @Test
    public void sendWithInterval_virtual_time_test() {
        StepVerifier.withVirtualTime(() -> sendWithInterval())
                .expectSubscription()
                .then(() ->
                        VirtualTimeScheduler.get().advanceTimeBy(Duration.ofMinutes(3))
                )
                .expectNext("a", "b", "c")
                .expectComplete()
                .verify();
    }

    @Test
    public void sendWithInterval_thenAwait_test() {
        Duration took = StepVerifier.withVirtualTime(() -> sendWithInterval())
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(3))
                .expectNext("a", "b", "c")
                .expectComplete()
                .verify();

        System.out.println("Verification time: " + took);
    }

    @Test
    public void thenAwait_test() {
        StepVerifier.withVirtualTime(() ->
                Flux.interval(Duration.ofMillis(0), Duration.ofMillis(1000))
                    .zipWith(Flux.just("a", "b", "c")).map(Tuple2::getT2)
                ).expectSubscription()
                .thenAwait() // 바로 실행
                .expectNext("a")
                .thenAwait(Duration.ofMillis(1000))
                .expectNext("b")
                .thenAwait(Duration.ofMillis(1000))
                .expectNext("c")
                .expectComplete()
                .verify();
    }

    @Test
    public void sendWithInterval_expectNoEvents_test() {
        StepVerifier.withVirtualTime(() -> sendWithInterval())
                .expectSubscription()
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNext("a")
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNext("b")
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNext("c")
                .expectComplete()
                .verify();
    }

    @Test
    public void context_test() {

    }
}
