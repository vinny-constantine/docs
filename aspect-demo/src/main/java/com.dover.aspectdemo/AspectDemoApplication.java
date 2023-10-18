package com.dover.aspectdemo;

import com.dover.aspectdemo.config.AspectConfig;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
//@ComponentScan(basePackages = {"com.dover", "com.kardo"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AspectConfig.class)})
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.dover"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AspectConfig.class)})
@SpringBootApplication
public class AspectDemoApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(AspectDemoApplication.class, args);
    }

}
