package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.util.concurrent.WaitStrategy;

import java.util.concurrent.Executors;

/**
 * @author dover
 * @since 2023/8/22
 */
public class WorkQueueProcessorDemo {


    @Test
    public void testWorkQueueProcessor() {
        // WorkQueueProcessor 支持多个订阅者，但事件会被所有订阅者瓜分消费（类似mq集群模式），通过轮询来交付事件给对应订阅者，
        WorkQueueProcessor<Long> data = WorkQueueProcessor.<Long>builder()
            .executor(Executors.newFixedThreadPool(2))
            .waitStrategy(WaitStrategy.blocking())
            .bufferSize(100)
            .build();
        data.subscribe(t -> System.out.println("1. " + t));
        data.subscribe(t -> System.out.println("2. " + t));
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
    }


}
