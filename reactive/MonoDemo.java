package com.dover.reactivedemo.mono;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @author dover
 * @since 2023/8/17
 */
public class MonoDemo {


    @Test
    public  void monoFromXxx() {
        // 生成一次值事件以及完成事件，即时值为多值，也会包装在一个对象中
        Mono.fromCallable(() -> new String[]{"color"}).subscribe(t -> System.out.println("received " + Arrays.toString(t)));
        // 生成一次单值事件及完成事件
        Mono.fromSupplier(() -> 1);
        // 生成一次无值事件及完成事件
        Mono.fromRunnable(() -> System.out.println(" "))
            .subscribe(t -> System.out.println("received " + t), null, () -> System.out.println("Finished"));
    }

    @Test
    public void monoFrom() {
        // 将Flux 流转为Mono 流，仅会从中获取第一个值
        Mono.from(Flux.just("Red", "Blue", "Yellow", "Black")).subscribe(t -> System.out.println("received " + t));
    }
}
