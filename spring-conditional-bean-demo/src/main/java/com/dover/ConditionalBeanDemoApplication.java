package com.dover;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.dover", "com.kardo"})
public class ConditionalBeanDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConditionalBeanDemoApplication.class, args);
    }
}