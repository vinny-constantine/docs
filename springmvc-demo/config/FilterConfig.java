package com.dover.springmvcdemo.config;

import com.alibaba.fastjson.serializer.DateCodec;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;


@Configuration
public class FilterConfig {


    @Bean
    public FilterRegistrationBean<HeaderFilter> filterRegistrationBean() {
        FilterRegistrationBean<HeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HeaderFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }


    public static class HeaderFilter implements Filter {

        @Override
        public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {

        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            String userId = req.getHeader("userId");
            chain.doFilter(req, response);
        }

        @Override
        public void destroy() {
        }
    }

    
    @Component
    @Order(Integer.MIN_VALUE)
    @WebFilter(urlPatterns = "/*")
    public static class TraceIdFilter implements Filter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            try {
                //设置traceId
                TraceIdUtil.setTraceId();
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                TraceIdUtil.removeTraceId();
            }
        }
    }
}
