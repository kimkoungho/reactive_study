package com.study.spring5reactive.serach_engine;

import rx.Observable;

import java.net.URL;


@SuppressWarnings("unused")
public interface RxSearchEngine {
    Observable<URL> search(String query, int limit);
}
