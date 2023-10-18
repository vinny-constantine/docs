package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;

/**
 * @author dover
 * @since 2023/8/22
 */
public class EmitterProcessorDemo {


    @Test
    public void testBackpressure() {
        // EmitterProcessor 支持背压，支持多个订阅者，每个订阅者依据自己的消费速度来获取Value事件
        UnicastProcessor<Long> data = UnicastProcessor.create();
        data.subscribe(t -> {
            System.out.println(t);
        });
        // sink 中的方法是线程安全的
        data.sink().next(10L);
    }

    @Test
    public void testConsume() {
        EmitterProcessor<Long> data = EmitterProcessor.create(1);
        data.subscribe(t -> System.out.println(t));
        // 通过 sink 来发布事件
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
        data.subscribe(t -> System.out.println("==2:" + t));
        sink.next(13L);
        sink.next(14L);
        sink.next(15L);
    }


}
