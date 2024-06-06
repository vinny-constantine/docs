
/**
 * 与 @RequestBody 注解作用相同
 * 解析参数并注入操作人相关信息，解析参数由 @RequestBody 的参数解析器完成
 *
 * @author wangwei
 * @date 2024/5/28
 * @see InjectOprInfoInterceptor
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectOprInfo {

    /**
     * 操作人用户ID字段名
     */
    @AliasFor("oprUserId")
    String value() default "operateUserId";

    /**
     * 操作人用户ID字段名
     */
    @AliasFor("value")
    String oprUserId() default "operateUserId";

    /**
     * 操作人用户名字段名
     */
    String oprUserName() default "operateUserName";

    /**
     * 操作人姓名字段名
     */
    String oprRealName() default "operateRealName";
}
