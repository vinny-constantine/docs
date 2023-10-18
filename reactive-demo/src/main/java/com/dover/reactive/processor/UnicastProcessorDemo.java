package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.UnicastProcessor;

/**
 * @author dover
 * @since 2023/8/22
 */
public class UnicastProcessorDemo {


    @Test
    public void testUnicastProcessor() {
        // UnicastProcessor 支持背压，内部会通过一个队列来缓存未被消费的事件，但仅支持一个订阅者
        UnicastProcessor<Long> data = UnicastProcessor.create();
        data.subscribe(t -> {
            System.out.println(t);
        });
        // sink 中的方法是线程安全的
        data.sink().next(10L);
    }


}
