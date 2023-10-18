package com.dover.aspectdemo.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * @author dover
 * @since 2022/7/29
 */
@Service
public class FooService2 {

    @Resource
    private FooService1 fooService1;

    public FooService2() {
        System.out.println("FooService2 created...");
    }

    @Async
    public void test() {

    }

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(encryptPhoneNo("123456766"));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdReserveOrderIdAndSalerIdBO implements Serializable {

        public String ordReserveOrderId;

        public String corpSalerId;
    }

    public static String encryptPhoneNo(String mobile) {
        if (StringUtils.isEmpty(mobile) || mobile.length() <= 5) {
            return mobile.charAt(0) + "****" + mobile.charAt(mobile.length() - 1);
        }
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();
        for (int i = 0, j = mobile.length() - 1; i + 4 < j; i++, j--) {
            prefix.append(mobile.charAt(i));
            suffix.append(mobile.charAt(j));
        }
        return prefix + "****" + suffix.reverse();
    }

}
