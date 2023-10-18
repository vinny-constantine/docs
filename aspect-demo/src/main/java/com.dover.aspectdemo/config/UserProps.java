package com.dover.aspectdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author dover
 * @since 2022/8/26
 */
@Setter
@Getter
@Component
@ConfigurationProperties("common")
public class UserProps {

    private Map<String, String> user;

}
