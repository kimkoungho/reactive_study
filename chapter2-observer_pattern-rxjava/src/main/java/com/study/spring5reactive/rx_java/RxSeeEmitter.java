package com.study.spring5reactive.rx_java;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rx.Subscriber;

import java.io.IOException;

public class RxSeeEmitter extends SseEmitter {
    static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
    private final Subscriber<Temperature> subscriber; // 구독자 캡슐화

    RxSeeEmitter() {
        super(SSE_SESSION_TIMEOUT);
        // 구독자 생성
        this.subscriber = new Subscriber<Temperature>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Temperature temperature) {
                try {
                    // Sse 클라이언트로 전송
                    RxSeeEmitter.this.send(temperature);
                } catch (IOException ioe) {
                    unsubscribe();
                }
            }
        };

        // 세션 완료, 타임 아웃에 대한 처리
        onCompletion(subscriber::unsubscribe);
        onTimeout(subscriber::unsubscribe);
    }

    Subscriber<Temperature> getSubscriber() {
        return subscriber;
    }
}
