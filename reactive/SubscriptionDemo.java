package com.dover.reactivedemo.subscribe;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * @author dover
 * @since 2023/8/18
 */
public class SubscribeDemo {

    @Test
    public void testBaseSubscribe() {
        // 四种订阅钩子函数
        BaseSubscriber<Long> fibonacciSubsciber = new BaseSubscriber<Long>() {
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
        fibonacciSubsciber.request(10);
        // 可忽略backpressure
        fibonacciSubsciber.requestUnbounded();
    }
}
