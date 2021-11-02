# note

## jvm

- 

## Spring

- bean 生命周期
  - AbstractApplicationContext.refresh
  - 扫描bean：通过 PathMatchingResourcePatternResolver 解析扫描路径里的 Resource

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
