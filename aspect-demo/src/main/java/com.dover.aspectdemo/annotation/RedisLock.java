
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MesRedisLock {

    /**
     * 获取lockKey的表达式，使用该表达式从入参中获取lockKey，支持SpEL表达式
     * 若为空字符串，则视为使用固定key，要求 keyPrefix 不能为空字符串
     * 获取对象的属性值作为lockKey，如："#user.id"，被拦截方法为 public void save(User user)
     * 获取数组或集合的元素值作为lockKey，如："#userList[0].id"，被拦截方法为 public void save(List<User> userList)
     * 使用方法来计算lockKey，如：T(com.zczy.mes.common.enums.LockName).CONFIG_MATCH_STRATEGY.key(#paramDto.tenantId, #paramDto.strategyCode)
     */
    String value();

    /**
     * 加锁的key前缀，如："mes_demand_lock:user:"
     */
    String keyPrefix() default "";

    /**
     * 获取锁失败后返回的消息
     */
    String message() default "上次操作尚未结束，请稍后再试";

    long waitTime() default 1;

    long leaseTime() default 10;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
