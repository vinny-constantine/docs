package com.dover.aspectdemo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dover
 * @since 2022/7/29
 */
@Service
@ConditionalOnBean(FooService1.class)
public class FooService3 {

    @Resource
    private FooService1 fooService1;

    public FooService3() {
        System.out.println("FooService3 created...");
    }
}
