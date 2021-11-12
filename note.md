# note

## jvm

- gc
  - 分代收集理论：
    - 弱分代假说：大部分对象是朝生暮死
    - 强分代假说：熬过gc次数越多的的对象越难消亡
    - 跨代引用假说：跨代引用相较于同代引用占比极低
  - 垃圾收集算法：引用计数、可达性分析
  - 垃圾收集策略：
    - 标记清除
      - 内存空间碎片化严重，需要额外的内存分配器，访问器，譬如“分区空闲分配链表”来保证内存空间逻辑上的连续性，相当于硬盘分区表
      - 有更低的*延迟*，因为不会出现*STW*，CMS收集器即使用该策略
    - 标记复制
      - 可能出现 survivor 区内存空间不够存放存活对象，因此出现了分配担保（handle promotion）策略，由老年代来保证存活对象不会丢失
    - 标记整理
      - 整理移动老年代对象可能出现 *stop the world*
      - 有更高的*吞吐量*，因为内存分配、访问频率远高于 gc 频率，会严重影响吞吐量，Parallel Scavenge收集器使用该策略
  - 新生代（young）
    - 使用 *标记复制* 进行回收
    - 垃圾回收称为 young gc / minor gc
    - 分为 eden 区 和 survivor1 和 survivor2， 默认比例为：8-1-1
  - 老年代（old）
    - 使用 *标记整理* 进行回收
    - 垃圾回收称为 major gc
  - 可达性分析
    - GC Root 枚举
      - 方法栈变量
      - 类静态常量
    - 解决跨代引用
      - 卡表
      - 卡页

## Spring

- bean 生命周期
  - AbstractApplicationContext.refresh
  - 扫描 bean：通过 PathMatchingResourcePatternResolver 解析扫描路径里的 Resource
  - 扫描的 bean 信息包装为 BeanDefinition
- 依赖注入自身，避免自调用时切面失效
  - 直接`@Autowired`或`@Resource`自身，并不一定有效，依赖注入进来的 bean 不一定被 `AnnotationAwareAspectJAutoProxyCreator` 代理过，
  （因为被早期引用，被标记为 earlyReferenceBean ，导致再次创建的 bean 被 AnnotationAwareAspectJAutoProxyCreator 忽略）
  没被代理的 bean 不会被 `AutowiredAnnotationBeanPostProcessor` 处理，导致没有进行依赖注入

## SpringMVC

- `@RequestParam`
  - 绑定pojo时可以省略
  - 绑定Map时不可省略
- `@DateTimeFormat(pattern="yyyy-MM-dd")`
  - 必须引入**joda-time**才可以生效
  - 标注在实体的属性上，可以对Date类型进行参数绑定

## mybatis

- update 语句影响行数：需要jdbc连接参数设置`useAffectedRows=true`，并且 xml 里的 sql 一定要使用 update 标签包裹

## Json

- jackson的`@JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")`需要加上时区

## mysql

- 索引
  - 索引失效
    - 不满足最左匹配原则
    - 使用函数
    - 类型转化
      - 显式类型转化：
      - 隐式类型转化容易被忽视，比如 shop_id 是 varchar 类型 shopId 传进去是 Long 类型，导致 `where shop_id = #{shopId}` 时索引失效
