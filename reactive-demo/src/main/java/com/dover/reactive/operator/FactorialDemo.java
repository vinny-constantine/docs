package com.dover.reactivedemo.operator;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

/**
 * @author dover
 * @since 2023/8/18
 */
public class FactorialDemo {


    Flux<Double> generateFactorial(long number) {
        Flux<Double> factorialStream = Flux.generate(() -> Tuples.of(0L, 1.0d), (state, sink) -> {
            Long factNumber = state.getT1();
            Double factValue = state.getT2();
            if (factNumber <= number) sink.next(factValue);
            else sink.complete();
            return Tuples.of(factNumber + 1, (factNumber + 1) * factValue);
        });
        return factorialStream;
    }
}
