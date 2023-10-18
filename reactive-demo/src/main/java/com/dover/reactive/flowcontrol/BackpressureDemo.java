package com.dover.reactivedemo.flowcontrol;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class BackpressureDemo {

    @Test
    public void testBackPressure() throws Exception {
        // 背压，当生产者发布的事件超出订阅者所需时，有多种溢出策略可选择
        // BUFFER：将未交付的事件缓存，当订阅者再次请求时再交付，这是默认策略
        // IGNORE：忽略背压，持续向订阅者交付事件
        // DROP：丢弃未交付的事件
        // LATEST：新的事件会缓存在旧事件前面，订阅者总是先消费最新的事件
        // ERROR：直接抛出异常
        Flux<Integer> numberGenerator = Flux.create(x -> {
            System.out.println("Requested Events :" + x.requestedFromDownstream());
            int number = 1;
            // 订阅者仅需要一个事件，但 publisher 发布100个事件
            while (number < 100) {
                x.next(number);
                number++;
            }
            System.out.println("=======complete");
            x.complete();
        });
        CountDownLatch latch = new CountDownLatch(1);
        numberGenerator.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // 仅需要一个事件
                request(1);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println(value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                throwable.printStackTrace();
                System.out.println("=============countDown");
                latch.countDown();
            }

            @Override
            protected void hookOnComplete() {
                // 发布者发布的事件超出订阅者所需，多余事件被Reactor框架缓存在队列中，因此 complete 事件被阻塞，无法消费到
                System.out.println("=============countDown");
                latch.countDown();
            }
        });
        // 因为始终消费不到“ERROR事件”和“COMPLETE事件”，因此无法 countDown，此处测试失败
        assertTrue(latch.await(1L, TimeUnit.SECONDS));
    }

    @Test
    public void testErrorBackPressure() throws Exception {
        // 使用Error策略
        Flux<Integer> numberGenerator = Flux.create(x -> {
            System.out.println("Requested Events :" + x.requestedFromDownstream());
            int number = 1;
            while (number < 100) {
                x.next(number);
                number++;
            }
            x.complete();
        }, FluxSink.OverflowStrategy.ERROR);
        // 由于生成者发布的事件数量超出订阅者所需，在 ERROR 策略下，程序直接抛出异常
        numberGenerator.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(1);
            }
        });
    }


    @Test
    public void testBackPressureOps() throws Exception {
        Flux<Integer> numberGenerator = Flux.create(x -> {
            System.out.println("Requested Events :" + x.requestedFromDownstream());
            int number = 1;
            while (number < 100) {
                x.next(number);
                number++;
            }
            x.complete();
        });
        CountDownLatch latch = new CountDownLatch(1);
        // 订阅前变更背压策略为 DROP，超出订阅者所需的事件均被丢弃
        numberGenerator.onBackpressureDrop(x -> System.out.println("Dropped :" + x))
//            .onBackpressureLatest()
//            .onBackpressureError()
//            .onBackpressureBuffer(100)
//            .onBackpressureBuffer(100, BufferOverflowStrategy.DROP_LATEST) // 当缓冲区满了之后，丢弃最新的事件
//            .onBackpressureBuffer(100, BufferOverflowStrategy.DROP_OLDEST) // 当缓冲区满了之后，丢弃最旧的事件
//            .onBackpressureBuffer(100, BufferOverflowStrategy.ERROR) // 当缓冲区满了之后，抛出异常
            .subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1);
                }
            });
        assertTrue(latch.await(1L, TimeUnit.SECONDS));
    }

}
