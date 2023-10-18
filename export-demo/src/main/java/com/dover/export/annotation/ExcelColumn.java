/*
 *
 *  * Copyright (C) 2018, All rights Reserved, Designed By www.xiniaoyun.com
 *  * @author: 王兴
 *  * @since 19-5-11 上午11:55 下午3:35
 *  * @Copyright: 2019 www.xiniaoyun.com Inc. All rights reserved.
 *  * 注意：本内容仅限于南京微欧科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 *
 */

package com.dover.export.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
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
