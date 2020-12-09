package com.study.spring5reactive.serach_engine;

import java.net.URL;
import java.util.Iterator;

@SuppressWarnings("unused")
public interface IterableSearchEngine {
    Iterator<URL> search(String query, int limit);
}
