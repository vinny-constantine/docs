package com.dover.reactivedemo.publisher;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.function.Tuples;

import java.util.concurrent.CountDownLatch;

/**
 * @author dover
 * @since 2023/8/23
 */
public class ColdAndHotPublishDemo {


    @Test
    public void testCodePub() {
        // 冷发布，只有存在订阅关系，生产者才会生产数据
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L), (state, sink) -> {
            sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator.take(5).subscribe(t -> System.out.println("1. " + t));
        fibonacciGenerator.take(5).subscribe(t -> System.out.println("2. " + t));
    }


    @Test
    public void testHotPub() {
        // 热发布，不论是否存在订阅关系，生产者都会产生数据，比如 processor 作为 publisher，新的订阅者也能收到已经发布过的数据
        UnicastProcessor<Long> hotSource = UnicastProcessor.create();
        Flux<Long> hotFlux = hotSource.publish().autoConnect();
        hotFlux.take(5).subscribe(t -> System.out.println("1. " + t));
        CountDownLatch latch = new CountDownLatch(2);
        new Thread(() -> {
            int c1 = 0, c2 = 1;
            while (c1 < 1000) {
                hotSource.onNext(Long.valueOf(c1));
                int sum = c1 + c2;
                c1 = c2;
                c2 = sum;
                if (c1 == 144) {
                    hotFlux.subscribe(t -> System.out.println("2. " + t));
                }
            }
            hotSource.onComplete();
            latch.countDown();
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
