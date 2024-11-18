

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;


@Slf4j
@Aspect
@Component
public class RedisLockAspect implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    //参数名解析器（需要配置maven-compiler-plugin的编译参数"-parameter"）
    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();
    //lockKey多值时的分隔符
    private static final String DELIMITER = "::";
    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(mesRedisLock)")
    public void pointCut(MesRedisLock mesRedisLock) {
    }
    
    @Around(value = "pointCut(mesRedisLock)", argNames = "joinPoint,mesRedisLock")
    public Object around(ProceedingJoinPoint joinPoint, MesRedisLock mesRedisLock) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        //尝试获取lockKey
        String lockKey = extractLockKey(joinPoint, mesRedisLock, args);
        try {
            //尝试获取锁
            log.info("===尝试获取锁，lockKey={}===", lockKey);
            boolean locked;
            if (lockKey.contains(DELIMITER)) {//lockKey多值时，每个key都尝试获取锁，所有锁都获取成功才视为锁成功
                locked = Arrays.stream(lockKey.split(DELIMITER)).allMatch(key -> redisLock.tryLock(key, mesRedisLock.waitTime(), mesRedisLock.leaseTime()));
            } else {
                locked = redisLock.tryLock(lockKey, mesRedisLock.waitTime(), mesRedisLock.leaseTime());
            }
            if (!locked) {//获取锁失败则返回异常
                if (lockKey.contains(DELIMITER)) {//lockKey多值时，获取锁失败，释放所有锁
                    Arrays.stream(lockKey.split(DELIMITER)).filter(key -> redisLock.isLockedByCurrentThread(key)).forEach(key -> redisLock.unlock(key));
                }
                if (MesResultPage.class.isAssignableFrom(returnType)) {
                    log.info("获取锁失败，返回MesResultPage失败结果");
                    return MesResultPage.fail(mesRedisLock.message());
                }
                log.info("获取锁失败，返回MesResultBean失败结果");
                return MesResultBean.fail(mesRedisLock.message());
            }
            log.info("===获取锁成功===");
            return joinPoint.proceed(args);
        } finally {
            try {
                if (lockKey.contains(DELIMITER)) {//lockKey多值时，释放所有锁
                    Arrays.stream(lockKey.split(DELIMITER)).filter(key -> redisLock.isLockedByCurrentThread(key)).forEach(key -> redisLock.unlock(key));
                    log.info("===释放锁成功，lockKey：{}===", lockKey);
                } else if (redisLock.isLockedByCurrentThread(lockKey)) {
                    redisLock.unlock(lockKey);
                    log.info("===释放锁成功，lockKey：{}===", lockKey);
                }
            } catch (Exception e) {
                log.error("释放锁失败，lockKey：{}", lockKey, e);
            }
        }
    }
    
    /**
     * 解析lockKey
     */
    private String extractLockKey(ProceedingJoinPoint joinPoint, MesRedisLock mesRedisLock, Object[] args) {
        String lockKey;
        String expression = mesRedisLock.value();
        String keyPrefix = mesRedisLock.keyPrefix();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (StringUtils.isNotBlank(expression)) {//解析表达式
            try {
                SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
                Expression spel = spelExpressionParser.parseExpression(expression);
                StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
                standardEvaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
                try {
                    log.info("使用参数名解析lockKey");
                    String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
                    if (parameterNames != null) {
                        for (int i = 0; i < args.length && i < parameterNames.length; i++) {
                            standardEvaluationContext.setVariable(parameterNames[i], args[i]);
                        }
                    }
                    lockKey = handleLockKey(spel.getValue(standardEvaluationContext), keyPrefix);
                    if (StringUtils.isNotBlank(lockKey)) return lockKey;
                } catch (Exception e) {
                    log.info("使用参数名解析lockKey失败，尝试直接从参数中解析");
                }
                log.info("使用参数作为root解析lockKey");
                for (Object arg : args) {
                    try {
                        standardEvaluationContext.setRootObject(arg);
                        lockKey = handleLockKey(spel.getValue(standardEvaluationContext), keyPrefix);
                        if (StringUtils.isNotBlank(lockKey)) return lockKey;
                    } catch (Exception e) {
                        log.info("解析lockKey失败，尝试从下一个参数中解析");
                    }
                }
            } catch (Exception e) {
                log.error("解析lockKey失败", e);
            }
            log.error("尝试解析lockKey失败，methodName={}，args={}，keyPrefix={}，value={}", signature.getName(), JSON.toJSONString(args), keyPrefix, mesRedisLock.value());
        }
        if (StringUtils.isNotBlank(keyPrefix)) {//表达式为空，尝试使用keyPrefix作为默认lockKey
            log.info("使用keyPrefix作为lockKey，keyPrefix={}", keyPrefix);
            return keyPrefix;
        }
        log.info("keyPrefix为空，使用方法名作为默认lockKey");
        return keyPrefix + signature.getName();
    }

    /**
     * 处理返回值
     */
    public static String handleLockKey(Object lockKeyObj, String keyPrefix) {
        if (lockKeyObj == null) {
            return null;
        }
        if (lockKeyObj instanceof String) {
            log.info("解析lockKey为String，直接作为lockKey");
            return keyPrefix + lockKeyObj;
        }
        if (lockKeyObj instanceof Collection) {
            log.info("解析lockKey为Collection，拼接作为lockKey");
            Collection<?> lockKeyCollection = (Collection<?>) lockKeyObj;
            return lockKeyCollection.stream().filter(Objects::nonNull)
                .map(key -> (key instanceof String || key instanceof Number) ? key.toString() : DigestUtil.md5Hex(JSON.toJSONString(key)))
                .map(key -> keyPrefix + key)
                .collect(Collectors.joining(DELIMITER));
        }
        log.info("解析lockKey为Object，使用md5生成lockKey");
        return keyPrefix + DigestUtil.md5Hex(JSON.toJSONString(lockKeyObj));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}