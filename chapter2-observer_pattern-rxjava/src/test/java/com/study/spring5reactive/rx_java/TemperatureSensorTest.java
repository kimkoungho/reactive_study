package com.study.spring5reactive.rx_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TemperatureSensorTest {

    @Autowired
    private TemperatureSensor temperatureSensor;

    @Test
    void temperatureStream() throws InterruptedException {
        temperatureSensor.temperatureStream()
                .subscribe(System.out::println);

        Thread.sleep(10000);
    }

    @Test
    void temperatureStream_no_subscribe() throws InterruptedException {
        temperatureSensor.temperatureStream();

        Thread.sleep(10000);
    }
}