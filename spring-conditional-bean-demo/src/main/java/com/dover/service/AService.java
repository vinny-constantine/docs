package com.dover.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dover
 * @since 2023/3/29
 */
@Service
@ConditionalOnBean(BService.class)
public class AService {

    @Resource
    private BService bService;

    public AService() {
        System.out.println("AService created...");
    }
}
