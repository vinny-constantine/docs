# reactive

## 概念
- Reactive 的宣言是什么？
  - message driven，reactive 系统应该由松耦合的组件构成，并且它们依赖异步的事件驱动
  - responsive，reactive 系统一定要各自响应用户输入
  - resilient，reactive 系统需要将异常隔离在各自系统内部
  - scalable，reactive 系统一定具备可伸缩性，部署多个副本来应对复杂的负载环境

- Reactive Extensions 是什么？
协助构建异步、事件驱动的网络交互的类库即为reactive extension，如 ReactiveX

- Reactive Stream specification 的关注点是什么？
- Reactive Stream 是基于什么准则完成的？
- Reactor Framework 最突出的特色是什么？

## 发展历程

- 第一代
- 第二代
- 第三代
- 第四代
- 第五代

## 示例

- 关注变化，并及时响应，事件驱动，避免回调地狱
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