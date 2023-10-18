package com.dover.reactivedemo.processor;

import org.junit.Test;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.TopicProcessor;
import reactor.util.concurrent.WaitStrategy;

import java.util.concurrent.Executors;

/**
 * @author dover
 * @since 2023/8/22
 */
public class TopicProcessorDemo {


    @Test
    public void testTopicProcessor() {
        // TopicProcessor 支持多个订阅者，会交付所有事件给每一个订阅者（类似于广播模式），通过事件循环机制实现，异步并发方式交付事件，RingBuffer 结构来支持背压
        // RingBuffer 的等待策略分为：blocking（阻塞，适用于对吞吐量和低延迟要求不高，不如CPU资源重要的场景）、busySpin（自旋，消耗CPU资源来避免系统调用，非常适合线程可绑定CPU核心的场景）、
        TopicProcessor<Long> data = TopicProcessor.<Long>builder()
            .executor(Executors.newFixedThreadPool(2))
            .bufferSize(8)
            .waitStrategy(WaitStrategy.busySpin())
            .build();
        data.subscribe(t -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("订阅者1：========" + t);
        });
        data.subscribe(t -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("订阅者2：========" + t);
        });
        FluxSink<Long> sink = data.sink();
        for (int i = 0; i < 20; i++) {
            sink.next((long) i);
        }
        // 主线程等待
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
