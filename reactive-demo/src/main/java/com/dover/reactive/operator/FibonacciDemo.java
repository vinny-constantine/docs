package com.dover.reactivedemo.operator;

import com.dover.reactivedemo.publisher.FluxDemo;
import com.dover.reactivedemo.util.Factorization;
import com.dover.reactivedemo.util.RomanNumber;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dover
 * @since 2023/8/17
 */
public class FibonacciDemo {


    @Test
    public void testConcatWith() {
        // concatWith：将多个流合并为一个流
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        fibonacciGenerator.take(10).concatWith(Flux.just(new Long[]{-1L, -2L, -3L, -4L})).subscribe(t -> {
            System.out.println(t);
        });
    }


    @Test
    public void testReduce() {
        // reduce：将流中所有值聚合为一个单值
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        fibonacciGenerator.take(10).reduce((x, y) -> x + y).subscribe(t -> {
            System.out.println(t);
        });
    }


    @Test
    public void testCollect() {
        // 收集斐波那契数列的前十个值
        System.out.println("====================收集斐波那契数列的前十个值======================");
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        fibonacciGenerator.take(10).collectList().subscribe(t -> {
            System.out.println(t);
        });
        // 将斐波那契数列的前十个值收集为有序的list集合
        System.out.println("====================将斐波那契数列的前十个值收集为倒序的list集合======================");
        fibonacciGenerator.take(10).collectSortedList((x, y) -> -1 * Long.compare(x, y)).subscribe(t -> {
            System.out.println(t);
        });
        // 将斐波那契数列的前十个值收集为map集合
        System.out.println("====================将斐波那契数列的前十个值收集为奇偶map集合======================");
        fibonacciGenerator.take(10).collectMap(t -> t % 2 == 0 ? "even" : "odd").subscribe(t -> {
            System.out.println(t);
        });
        // 将斐波那契数列的前十个值收集为map集合
        System.out.println("====================将斐波那契数列的前十个值收集为奇偶map集合======================");
        fibonacciGenerator.take(10).collectMultimap(t -> t % 2 == 0 ? "even" : "odd").subscribe(t -> {
            System.out.println(t);
        });
    }

    @Test
    public void testRepeat() {
        // 将斐波那契数列流前十个值，重复一次
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        fibonacciGenerator.take(10).repeat(2).subscribe(t -> {
            System.out.println(t);
        });
    }


    @Test
    public void testFlatMap() {
        // 展开因子集合，将所有生成的斐波那契数列的每一项的因子集合均合并展开
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        fibonacciGenerator.skip(1)
            .take(10)
            .flatMap(t -> Flux.fromIterable(Factorization.findfactor(t.intValue())))
            .subscribe(t -> {
                System.out.println(t);
            });
    }

    @Test
    public void testMap() {
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        // 转换为罗马数字
        System.out.println("====================转换为罗马数字======================");
        fibonacciGenerator.skip(1).take(10).map(t -> RomanNumber.toRomanNumeral(t.intValue())).subscribe(t -> {
            System.out.println(t);
        });
        System.out.println("====================查找因子======================");
        // 查找因子
        fibonacciGenerator.skip(1).take(10).map(t -> Factorization.findfactor(t.intValue())).subscribe(t -> {
            System.out.println(t);
        });
    }


    @Test
    public void testSkip() {
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        System.out.println("====================跳过前10======================");
        // 跳过前10
        fibonacciGenerator.skip(10).subscribe(t -> {
            System.out.println(t);
        });
        System.out.println("====================跳过前10======================");
        // 跳过10毫秒
        System.out.println("====================跳过10毫秒======================");
        fibonacciGenerator.skip(Duration.ofMillis(10)).subscribe(t -> {
            System.out.println(t);
        });
        System.out.println("====================跳过10毫秒======================");
        // 跳过直至值大于100
        System.out.println("====================跳过直至值大于100======================");
        fibonacciGenerator.skipUntil(t -> t > 100).subscribe(t -> {
            System.out.println(t);
        });
        System.out.println("====================跳过直至值大于100======================");
    }


    @Test
    public void testFilter() {
        Flux<Long> fibonacciGenerator = FluxDemo.buildFibonacciGenerator();
        // 同步方式，得到偶数
        fibonacciGenerator.filter(x -> (x & 1) == 0).subscribe(System.out::println);
        System.out.println("===============================================");
        // 异步方式，判断mono是否为true
        fibonacciGenerator.filterWhen(x -> Mono.just(x > 100)).subscribe(System.out::println);
    }


    @Test
    public void testFibonacciFluxSink() {
        // fluxSink 可异步生成任意数量的事件，不关注 backpressure ，也不关注订阅关系，即使订阅关系废弃，也能继续生产事件
        // fluxSink 的实现必须监听取消事件，以及显式初始化 stream 闭包
        Flux<Long> fibonacciGenerator = Flux.create(e -> {
            long current = 1, prev = 0;
            AtomicBoolean stop = new AtomicBoolean(false);
            e.onDispose(() -> {
                stop.set(true);
                System.out.println("******* Stop Received ****** ");
            });
            while (current > 0) {
                e.next(current);
                System.out.println("generated " + current);
                current = current + prev;
                prev = current - prev;
            }
            e.complete();
        });
        List<Long> fibonacciSeries = new LinkedList<>();
        fibonacciGenerator.take(50).subscribe(t -> {
            System.out.println("consuming " + t);
            fibonacciSeries.add(t);
        });
        System.out.println(fibonacciSeries);
    }

}
