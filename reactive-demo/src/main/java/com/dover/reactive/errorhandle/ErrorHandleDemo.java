package com.dover.reactivedemo.publisher;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dover
 * @since 2023/8/22
 */
public class FluxDemo {


    public static void main(String[] args) {
        Flux<Long> fibonacciGenerator = buildFibonacciGenerator();
        fibonacciGenerator.subscribe(System.out::println);
    }


    public static Flux<Long> buildFibonacciGenerator() {
        return Flux.create(e -> {
            long current = 1, prev = 0;
            AtomicBoolean stop = new AtomicBoolean(false);
            e.onDispose(() -> {
                stop.set(true);
                System.out.println("******* Stop Received ****** ");
            });
            while (current > 0) {
                e.next(current);
                System.out.println("generated " + current);
                current = current + prev;
                prev = current - prev;
            }
            e.complete();
        });
    }
}
