package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

/**
 * @author dover
 * @since 2023/8/22
 */
public class ReplayProcessorDemo {


    @Test
    public void testReplay() {
        // ReplayProcessor 具备缓存和重放事件的能力，当一个新的订阅者订阅后能重放指定数量的事件给该订阅者
        ReplayProcessor<Long> data = ReplayProcessor.create(4);
        data.subscribe(t -> System.out.println(t));
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
        sink.next(13L);
        sink.next(14L);
        data.subscribe(t -> System.out.println("==2:" + t));
    }


}
