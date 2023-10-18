package com.dover.aspectdemo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;

/**
 * @author dover
 * @since 2021/9/1
 */
@EnableAspectJAutoProxy
@Configuration(proxyBeanMethods = false)
public class AspectConfig {

    @Bean
    @Order(-1)
    public PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    @ConditionalOnProperty(name = "abcd", havingValue = "1")
    public AppProps appProps() {
        AppProps appProps = new AppProps();
        appProps.setName("123");
        return appProps;
    }

}
