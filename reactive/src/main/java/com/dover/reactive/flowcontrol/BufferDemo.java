package com.dover.reactivedemo.flowcontrol;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.time.Duration;

public class BufferDemo {

    @Test
    public void testBufferWithInfiniteSize() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator.take(100).buffer(10).subscribe(x -> System.out.println(x));
    }

    @Test
    public void testBufferSizes() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        // maxSize：每个缓冲区的最大容量，skipSize：构建新的缓冲区前必须跳过的事件数量
        // 当 maxSize > skipSize 时，缓冲区可能会重叠，意味着其中的事件可能会跨缓冲区重复
        // 当 maxSize < skipSize 时，缓冲区则不会相交, 意味着会丢失事件
        // 当 maxSize = skipSize 时，就相当于 buffer()
        fibonacciGenerator.take(100).buffer(6, 7).subscribe(x -> System.out.println(x));
    }

    @Test
    public void testBufferTimePeriod() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator.buffer(Duration.ofNanos(10)).subscribe(x -> System.out.println(x));
    }
}
