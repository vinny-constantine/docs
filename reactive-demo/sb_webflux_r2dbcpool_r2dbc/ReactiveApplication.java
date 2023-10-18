package com.example.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableConfigurationProperties(R2dbcProperties.class)
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }
}
