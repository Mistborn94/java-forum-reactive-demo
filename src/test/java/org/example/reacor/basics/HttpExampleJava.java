package org.example.reacor.basics;

import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

public class HttpExampleJava {

    private final HttpClient client = HttpClient.newBuilder()
            .build();


    @Test
    public void httpGet() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        ;

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://httpbin.org/get?param1=a&param2=b"))
                .GET()
                .build();

        final CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        Mono.fromFuture(future)
                .doFinally(signalType -> latch.countDown())
                .subscribe(stringHttpResponse -> System.out.println("Http Response: " + stringHttpResponse));

        latch.await();
    }

    @Test
    public void httpPost() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        final Flux<ByteBuffer> requestBody = createFlux();
        final HttpRequest request = buildRequest(FlowAdapters.toFlowPublisher(requestBody));

        final var future = client.sendAsync(request, HttpResponse.BodyHandlers.ofPublisher());
        final Mono<HttpResponse<Flow.Publisher<List<ByteBuffer>>>> mono = Mono.fromFuture(future);

        mono.flatMapMany(value -> FlowAdapters.toPublisher(value.body()))
                .map(this::bodyToString)
                .doFinally(signalType -> latch.countDown())
                .subscribe(stringHttpResponse -> System.out.println("Http Response: " + stringHttpResponse));

        latch.await();
    }

    private HttpRequest buildRequest(Flow.Publisher<ByteBuffer> publisher) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://httpbin.org/post?param1=a&param2=b"))
                .POST(HttpRequest.BodyPublishers.fromPublisher(publisher))
                .build();
    }

    private String bodyToString(List<ByteBuffer> body) {
        return body.stream()
                .map(StandardCharsets.UTF_8::decode)
                .collect(Collectors.joining());
    }


    private Flux<ByteBuffer> createFlux() {
        return Flux.just("Hello", "World")
                .map(value -> ByteBuffer.wrap(value.getBytes()));
    }
}
