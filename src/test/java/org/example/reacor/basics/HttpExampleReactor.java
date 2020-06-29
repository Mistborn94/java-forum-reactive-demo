package org.example.reacor.basics;

import lombok.Data;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.time.Duration;

public class HttpExampleReactor {

    @Test
    public void httpGet() {
        final Mono<String> responseMono = HttpClient.create()
                .get()
                .uri("http://httpbin.org/get?param1=a&param2=b")
                .responseContent()
                .aggregate()
                .asString()
                .doOnNext(value -> System.out.println("Received String: " + value));

//        responseMono.subscribe(value -> System.out.println("Subscribed."));

        StepVerifier.create(responseMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void httpPost() {
        final Flux<String> requestData = Flux.just("Hello", "World");

        final Mono<String> responseMono = HttpClient.create()
                .post()
                .send(ByteBufFlux.fromString(requestData))
                .uri("http://httpbin.org/post?param1=a&param2=b")
                .responseContent()
                .aggregate()
                .asString()
                .doOnNext(value -> System.out.println("Received String: " + value));

//        responseMono.subscribe(value -> System.out.println("Subscribed."));

        StepVerifier.create(responseMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void combined() {
        Mono<String> requestA = makeGetRequest("http://httpbin.org/get?requestId=1")
                .delayElement(Duration.ofMillis(200))
                .doOnNext(value -> System.out.println("Value A: " + value));

        Mono<String> requestB = makeGetRequest("http://httpbin.org/get?requestId=2")
                .doOnNext(value -> System.out.println("Value B: " + value));

        var combined = Mono.zip(requestA, requestB, (a, b) -> new ResponsePair(a.trim(), b.trim()))
                .doOnNext(value -> System.out.println("Got combined value: " + value));

        StepVerifier.create(combined)
                .expectNextCount(1)
                .verifyComplete();
    }

    public Mono<String> makeGetRequest(String uri) {
        return HttpClient.create()
                .get()
                .uri(uri)
                .responseContent()
                .aggregate()
                .asString();
    }

    @Data
    static class ResponsePair {
        final String responseA;
        final String responseB;
    }
}
