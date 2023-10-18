package com.dover.reactivedemo.subscriber;

import com.dover.reactivedemo.publisher.FluxDemo;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author dover
 * @since 2023/8/18
 */
public class SubscriberDemo {


    @Test
    public void testSubscribe() {
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        // 不消费
        fibonacciGenerator.take(10).subscribe();
        // 仅消费值事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t));
        // 消费值事件，并打印异常事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t), e -> e.printStackTrace());
        // 消费值事件，异常事件，完成事件
        fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t) // 值事件
            , e -> e.printStackTrace() // 异常事件
            , () -> System.out.println("Finished") // 完成事件
        );
        // 消费值事件，异常事件，完成事件，订阅事件，返回的 disposable 可用于取消订阅
        Disposable disposable = fibonacciGenerator.take(10).subscribe(t -> System.out.println("consuming " + t) // Value事件
            , e -> e.printStackTrace() // 异常事件
            , () -> System.out.println("Finished") // 完成事件
            , s -> System.out.println("Subscribed :" + s) // 订阅事件
        );
        // 触发取消事件
        disposable.dispose();

    }

    @Test
    public void testBaseSubscribe() {
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        // 五种钩子函数
        BaseSubscriber<Long> fibonacciSubscriber = new BaseSubscriber<Long>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
            }
            @Override
            protected void hookOnNext(Long value) {
            }
            @Override
            protected void hookOnComplete() {
            }
            @Override
            protected void hookOnError(Throwable throwable) {
            }
            @Override
            protected void hookOnCancel() {
            }
        };
        // 结交订阅关系
        fibonacciGenerator.subscribe(fibonacciSubscriber);
        // 订阅者主动拉取10个Value事件，触发 Request 事件
        fibonacciSubscriber.request(10);
        // 可忽略backpressure
        fibonacciSubscriber.requestUnbounded();
    }
}
