# reactive


## 概念
- Reactive 的宣言是什么？
  - message driven，reactive 系统应该由松耦合的组件构成，并且它们依赖异步的事件驱动
  - responsive，reactive 系统一定要各自响应用户输入
  - resilient，reactive 系统需要将异常隔离在各自系统内部
  - scalable，reactive 系统一定具备可伸缩性，部署多个副本来应对复杂的负载环境

- Reactive Extensions 是什么？
协助构建异步执行、事件驱动的网络交互的类库即为reactive extension，如 ReactiveX

- Reactive Stream specification 的关注点是什么？
- Reactive Stream 是基于什么准则完成的？
- Reactor Framework 最突出的特色是什么？


## 发展历程

- 第一代
- 第二代
- 第三代
- 第四代
- 第五代

## 数据流

![数据流](./img/reactive-stream.png)

## 特性

### 事件驱动，避免回调地狱

#### 事件类型
- Subsription：订阅事件
- Value：值事件，发布者发布的单值事件
- Completion：流正常结束事件
- Error：流异常结束事件
- Cancel：取消订阅事件
- Request：请求事件，用于主动向发布者拉取数据

```java
```
### 响应式，关注变化，并及时响应
```java
// 普通java代码
int value1 = 5;
int value2 = 10;
int sum = val1 + val2;
System.out.println(sum); // 15
value1 = 15;
System.out.println(sum); // 15

// reactive java 代码
int value1 = 5;
int value2 = 10;
int sum = val1 + val2;
System.out.println(sum); // 15
value1 = 15;
System.out.println(sum); // 25
```
### 柔韧性(resilient)
### 伸缩性(scalable)
### 背压（backpressure）：
用于解决发布者与订阅者之间处理效率不一致而导致的事件堆积，内存溢出等问题
  - pull模式：即订阅方实现 backpressure
```java
@Test
public void testPullBackpressure(){
    Flux.just(1, 2, 3, 4)
        .subscribe(new Subscriber<Integer>() {
            private Subscription s;
            int onNextAmount;
            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(2);
            }
            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
                onNextAmount++;
                if (onNextAmount % 2 == 0) {
                    s.request(2);
                }
            }
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onComplete() {}
        });
    try {
        Thread.sleep(10*1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
 - push模式：即发布方实现 backpressure
```java
// 通过delayElements
@Test
public void testPushBackpressure() throws InterruptedException {
    Flux.range(1, 1000)
        .delayElements(Duration.ofMillis(200))
        .subscribe(e -> {
            LOGGER.info("subscribe:{}",e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
    Thread.sleep(100*1000);
}
// 通过buffer方法
@Test
public void testBufferBackpressure() throws InterruptedException {
    Flux.range(1, 1000)
        .buffer(Duration.ofMillis(800))
        .subscribe(e -> {
            LOGGER.info("subscribe:{}",e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
    Thread.sleep(100*1000);
}
// 通过take方法
@Test
public void testTakeBackpressure() throws InterruptedException {
    Flux.range(1, 1000)
        .take(Duration.ofMillis(4000))
        .subscribe(e -> {
            LOGGER.info("subscribe:{}",e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
    Thread.sleep(100*1000);
}
```



## Reactor

### 构造流，消费流

#### Flux
- flux.just()：最简单的构造流方式
- flux.fromXxx()
  - flux.fromArray()
  - flux.fromIterable()
- flux.create()：fluxSink 可异步生成任意数量的事件，不关注 backpressure ，也不关注订阅关系，即使订阅关系废弃，也能继续生产事件fluxSink 的实现必须监听取消事件，以及显式初始化 stream 闭包


#### Mono
最多生产一个事件，适用于一次响应的模型，比如：数据聚合、http请求响应，微服务调用等
Mono只能生产一下三种事件：Value、Completion、Error
- Mono.fromXxx()
  - Mono.fromCallable()
  - Mono.fromFuture()
- Mono.empty: 生成无值仅有完成事件的stream
- Mono.defer: 用于构建懒初始化的发布者，仅有订阅者结交订阅关系后才会生成发布者实例
- Mono.create: 构造MonoSink，用于生产一个“值事件”、“完成事件”、“异常事件”，同样也不关注 backpressure 和订阅关系

#### Processor

#### 冷发布、热发布
- 冷发布的数据是在订阅之后才产生的，如果没有订阅者，则不会产生数据
 - Flux 和 Mono 生成的发布者，即是冷发布
- 热发布，不论是否存在订阅关系，生产者都会产生数据，比如 processor 作为 publisher，新的订阅者也能收到已经发布过的数据
 - 某些 processor 比如 UnicastProcessor 转化的生产者便可以热发布

### 变换流（operator）

#### 数据过滤
- filter
```java
// 斐波那契数列流过滤得到偶数，同步方式
fibonacciGenerator.filter(a -> a%2 == 0).subscribe(t -> {
    System.out.println(t);
});
// 斐波那契数列流过滤得到小于10的数，异步方式
fibonacciGenerator.filterWhen(a -> Mono.just(a < 10)).subscribe(t -> {
    System.out.println(t);
});
```
- skip
```java
// 斐波那契数列流跳过前十个值
fibonacciGenerator.skip(10).subscribe(t -> {
    System.out.println(t);
});
// 斐波那契数列流跳过10毫秒产生的值
fibonacciGenerator.skip(Duration.ofMillis(10)).subscribe(t -> {
    System.out.println(t);
});
// 斐波那契数列流跳过小于100的值
fibonacciGenerator.skipUntil(t -> t > 100).subscribe(t -> {
    System.out.println(t);
});
```
- distinct：数据去重
- distinctUntilChanged：获取不重复的首个子集（一旦出现重复元素，则全部跳过）
- ignoreElements：忽略所有元素
- single：获取单个元素
- elmentAt：获取某个位置的元素

#### 数据映射
- map：将流中的每一个值都应用到映射器中
```java
// 将斐波那契数列，前十个数转换为罗马数字
RomanNumber numberConvertor= new RomanNumber();
fibonacciGenerator.skip(1).take(10).map(t-> numberConvertor.toRomanNumeral(t.intValue())).subscribe(t -> {
    System.out.println(t);
});

- flatMap：将多个流展开合并为一个流
```java
// 展开因子集合，将所有生成的斐波那契数列的每一项的因子集合均合并展开
Flux<Long> fibonacciGenerator = buildFibonacciGenerator();
fibonacciGenerator.skip(1).take(10).flatMap(t-> Flux.fromIterable(Factorization.findfactor(t.intValue()))).subscribe(t -> {
    System.out.println(t);
});
```

- repeat：在收到完成事件后再将流重复指定次数
```java
// 将斐波那契数列流前十个值，重复一次
fibonacciGenerator.take(10).repeat(2).subscribe(t -> {
    System.out.println(t);
}); 
```

- collect：将流中的数据收集为集合
```java
// 收集斐波那契数列的前十个值
Flux<Long> fibonacciGenerator = buildFibonacciGenerator();
fibonacciGenerator.take(10).collectList().subscribe(t -> {
    System.out.println(t);
});
```

- collectList：将流中数据收集为list集合
- collectSortedList：将流中数据收集为有序的list集合
- collectMap：将流中数据收集为map
```java
// 将斐波那契数列的前十个值收集为map集合
fibonacciGenerator.take(10).collectMap(t -> t % 2 == 0 ? "even" : "odd").subscribe(t -> {
    System.out.println(t);
});
```

- reduce：将流中所有值聚合为一个单值
```java
// 将斐波那契数列的前十个值相加
Flux<Long> fibonacciGenerator = buildFibonacciGenerator();
fibonacciGenerator.take(10).reduce((x, y) -> x + y).subscribe(t -> {
    System.out.println(t);
});

```
- any：流中任意值符合条件，则返回值为true的 `Mono<Boolean>`
- all：流中所有值符合条件，则返回值为true的 `Mono<Boolean>`
- concatWith：将其他流拼接在当前流之后合并
- startWith：将其他流拼接再当前流之前合并

## SpringWebFlux
SpringWebFlux支持两种构建 reactive 应用的模式，一是注解形式，二是函数式配置

![springWebFlux](./img/springwebflux.png)

### Jar包依赖
```groove
dependencies {
    compile 'org.springframework.boot:spring-boot-starter-webflux'    
}
```
### 注解形式
完全兼容 SpringWebMvc 的所有注解，诸如：`@RestController`、`@RequestMapping`、`@ResponseBody`
```java
@EnableWebFlux
@SpringBootApplication
public class ReactorMain { 
    public static void main(String[] args) {
        SpringApplication.run(ReactorMain.class, args);
    }
}

@RestController
public class ReactiveController {
    @GetMapping("/fibonacci")
    @ResponseBody 
    public Publisher<Long>fibonacciSeries() { 
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L), 
        (state, sink) -> {
            if (state.getT1() < 0){
                sink.complete();
            } else {
                sink.next(state.getT1());
            }
            return Tuples.of(state.getT2(), state.getT1() + state.getT2()); 
        });
        return fibonacciGenerator; 
    }
}
```
### 函数式配置
类似于在`@Configuration`中代码式配置 Bean
```java
@Configuration
public void WebFluxConfig {

    @Bean
    RouterFunction<ServerResponse> fibonacciEndpoint() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L),
        (state, sink) -> {
            if (state.getT1() < 0) sink.complete();
            else sink.next(state.getT1());
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        RouterFunction<ServerResponse> fibonacciRoute = RouterFunctions.route(RequestPredicates.path("/fibonacci"), request -> ServerResponse.ok()
            .body(BodyInserters.fromPublisher(fibonacciGenerator, Long.class)));
        return fibonacciRoute;
    }
}
```

