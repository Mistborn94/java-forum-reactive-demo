package org.example.reacor.basics;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class BasicsExample {

    @Test
    public void fluxTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);

        final Flux<Integer> integerFlux = Flux.just(1, 2, 3, 4, 5)
                .delayElements(Duration.ofMillis(200))
                .doOnNext(value -> log("Flux emitted " + value))
                .map(value -> value * 2)
                .doFirst(() -> log("Flux starting"))
                .doFinally(signal -> {
                    log("Flux finished");
                    latch.countDown();
                });

        final Flux<Object> emptyFlux = Flux.empty()
                .doFirst(() -> log("Empty Flux starting"))
                .doFinally(signal -> {
                    log("Empty Flux finished");
                    latch.countDown();
                });

        log("No subscription - Nothing has happened yet");

        integerFlux.subscribe(value -> log("Flux subscribed " + value));
        emptyFlux.subscribe(value -> log("Empty Flux subscribed " + value));

        latch.await();
    }

    @Test
    public void monoTest() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);

        final Mono<Integer> integerMono = Mono.just(1)
                .delayElement(Duration.ofMillis(200))
                .doOnNext(value -> log("Mono emitted " + value))
                .map(value -> value * 2)
                .doFirst(() -> log("Mono starting"))
                .doFinally(signal -> {
                    log("Mono finished");
                    latch.countDown();
                });

        final Mono<Object> emptyMono = Mono.empty()
                .doFirst(() -> log("Empty Mono starting"))

                .doFinally(signal -> {
                    log("Empty Mono finished");
                    latch.countDown();
                });

        log("No subscription - Nothing has happened yet");
        integerMono.subscribe(value -> log("Mono subscribed " + value));
        emptyMono.subscribe(value -> log("Empty Mono subscribed " + value));

        latch.await();
    }

    private void log(String s) {
        System.out.printf("[%s] - %s%n", Thread.currentThread().getName(), s);
    }
}
