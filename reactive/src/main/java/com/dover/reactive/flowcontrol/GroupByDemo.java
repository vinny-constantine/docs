package com.dover.reactivedemo.flowcontrol;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GroupByDemo {

    @Test
    public void testGrouping() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        // 取斐波那契数列前20，按可被2、3、5、7整除来分组，并打印结果
        fibonacciGenerator.take(20).groupBy(i -> {
            List<Integer> divisors = Arrays.asList(2, 3, 5, 7);
            Optional<Integer> divisor = divisors.stream().filter(d -> i % d == 0).findFirst();
            return divisor.map(x -> "可被[" + x +"]整除").orElse("其他");
        }).concatMap(x -> {
            System.out.println("\n" + x.key());
            return x;
        }).subscribe(x -> System.out.print(" " + x));
    }

}
