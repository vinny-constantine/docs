# note

## jvm

- 

## Spring

- bean 生命周期
  - AbstractApplicationContext.refresh
  - 扫描bean：通过 PathMatchingResourcePatternResolver 解析扫描路径里的 Resource
- 依赖注入自身，避免自调用时切面失效
  - 直接`@Autowired`或`@Resource`自身，并不一定有效，依赖注入进来的 bean 不一定被 `AnnotationAwareAspectJAutoProxyCreator` 代理过，没被代理的 bean 不会被 `AutowiredAnnotationBeanPostProcessor` 处理，导致没有进行依赖注入

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
