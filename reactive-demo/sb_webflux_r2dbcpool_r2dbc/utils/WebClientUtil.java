package com.example.reactive.utils;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * @author dover
 * @since 2023/9/1
 */
public class WebClientUtil {

    public void readFibonacciNumbers() {
        WebClient client = WebClient.create("http://localhost:8080");
        Flux<Long> result = client.get()
            .uri("/fibonacci")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(Long.class);
        result.subscribe(x -> System.out.println(x));
    }
}
