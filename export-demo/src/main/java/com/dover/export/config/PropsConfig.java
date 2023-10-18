package com.dover.export.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("dover.props")
public class PropsConfig {

    private Map<String, Export> map = new HashMap<>();

    @Data
    public static class Export {
        private Integer id;

        private String name;
    }
}
