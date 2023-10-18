package com.dover.reactivedemo.flowcontrol;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

public class WindowDemo {

    @Test
    public void testWindowsFixedSize() {
        // 与缓冲区类似，能够将`Flux<T>`生成的事件分割，但聚合的结果为Processor，每个分割得到的Processor都能重新发布订阅事件
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        // 将 Flux<T> 流每10个事件为一组分割为window，每个window都是一个 UnicastProcessor，所以需要用 concatMap 或 flatMap 将其组合起来，再订阅消费
        fibonacciGenerator.window(10).concatMap(x -> x).subscribe(x -> System.out.print(x + " "));
    }


    @Test
    public void testWindowsPredicate() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator.windowWhile(x -> x < 500).concatMap(x -> x).subscribe(x -> System.out.println(x));
    }


}
