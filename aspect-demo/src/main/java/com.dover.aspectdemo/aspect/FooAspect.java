package com.dover.aspectdemo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author dover
 * @since 2021/9/1
 */
@Aspect
@Configuration
public class FooAspect {

    @Value("${pointcut}")
    private String s = "";

    private final String pointcut = "execution(* *..*.run(..))";

    @Around("@annotation(com.dover.aspectdemo.annotation.Mark)")
    public Object run(ProceedingJoinPoint joinPoint) {
        try {
            System.out.println("around notify start...");
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
