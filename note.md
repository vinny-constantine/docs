# SpringMVC
* `@RequestParam`
  * 绑定pojo时可以省略
  * 绑定Map时不可省略
* `@DateTimeFormat(pattern="yyyy-MM-dd")`
  * 必须引入*joda-time*才可以生效
  * 标注在实体的属性上，可以对Date类型进行参数绑定
# Json
  * jackson的`@JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")`需要加上时区