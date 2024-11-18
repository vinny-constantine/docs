package com.dover.export.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dover
 * excel注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    @AliasFor("columnName") String value() default "";

    /**
     * 列排序值从 0 开始
     */
    int col() default 0;

    /**
     * 是否为主键列
     */
    boolean isKey() default false;

    /**
     * 是否展示
     */
    boolean show() default true;

    /**
     * 同 value
     */
    @AliasFor("value") String columnName() default "";
}
