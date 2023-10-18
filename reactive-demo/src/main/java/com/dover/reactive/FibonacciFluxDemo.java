package com.dover.reactivedemo.flux;

import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dover
 * @since 2023/8/17
 */
public class FibonacciFluxDemo {


    @Test
    public void testSubscribe() {
        Flux<Integer> fibonacciGenerator = Flux.create(e -> {
            int prev = 0;
            int current = 1;
            AtomicBoolean stop = new AtomicBoolean(false);
            e.onDispose(() -> {
                stop.set(true);
                System.out.println("=========== receive stop =============");
            });
            while (true) {
                e.next(current);
                System.out.println("generated " + current);
                current = current + prev;
                prev = current - prev;
                if (stop.get()) break;
            }
            e.complete();
        });
        // 不消费
        fibonacciGenerator.take(10).subscribe();
        // 仅消费值事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t));
        // 消费值事件，并打印异常事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t), e -> e.printStackTrace());
        // 消费值事件，异常事件，完成事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t), e -> e.printStackTrace(),
            () -> System.out.println("Finished"));
        // 消费值事件，异常事件，完成事件，订阅事件，返回的 disposable 可用于取消订阅
        Disposable disposable = fibonacciGenerator.take(10)
            .subscribe(t -> System.out.println("consuming " + t), e -> e.printStackTrace(),
                () -> System.out.println("Finished"), s -> System.out.println("Subscribed :" + s));
    }


    @Test
    public void testFibonacciFluxSink() {
        // fluxSink 可异步生成任意数量的事件，不关注 backpressure ，也不关注订阅关系，即使订阅关系废弃，也能继续生产事件
        // fluxSink 的实现必须监听取消事件，以及显式初始化 stream 闭包
        Flux<Long> fibonacciGenerator = Flux.create(e -> {
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
        List<Long> fibonacciSeries = new LinkedList<>();
        fibonacciGenerator.take(50).subscribe(t -> {
            System.out.println("consuming " + t);
            fibonacciSeries.add(t);
        });
        System.out.println(fibonacciSeries);
    }
}
