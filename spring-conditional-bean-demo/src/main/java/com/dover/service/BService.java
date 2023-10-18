package com.dover.service;

import com.kardo.CService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dover
 * @since 2023/3/29
 */
@Service
@ConditionalOnBean(CService.class)
public class BService {

    @Resource
    private CService cService;

    public BService() {
        System.out.println("BService created...");
    }
}
