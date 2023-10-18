package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.DirectProcessor;

/**
 * @author dover
 * @since 2023/8/22
 */
public class DirectProcessorDemo {


    @Test
    public void testBackpressure() {
        // DirectProcessor 无法处理背压，一旦超出订阅者所需事件数，则抛出异常
        DirectProcessor<Long> data = DirectProcessor.create();
        data.subscribe(t -> System.out.println(t), e -> e.printStackTrace(), () -> System.out.println("Finished"),
            s -> s.request(1));
        data.onNext(10L);
        data.onNext(11L);
        data.onNext(12L);
    }


    @Test
    public void testDirectProcessor() {
        // 一旦DirectProcessor收到完成事件，则不再接收任何data事件
        DirectProcessor<Long> data = DirectProcessor.create();
        data.subscribe(t -> System.out.println(t), e -> e.printStackTrace(), () -> System.out.println("Finished 1"));
        data.onNext(10L);
        data.onComplete();
        data.subscribe(t -> System.out.println(t), e -> e.printStackTrace(), () -> System.out.println("Finished 2"));
        data.onNext(12L);
    }

    @Test
    public void testProcessor() {
        DirectProcessor<Long> data = DirectProcessor.create();
        data.take(2).subscribe(t -> System.out.println(t));
        data.onNext(10L);
        data.onNext(11L);
        data.onNext(12L);
    }
}
