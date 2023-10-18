package com.dover.export.dao;

import com.dover.export.entity.OrderItemExportBO;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author dover
 * @since 2022/3/10
 */
@Service
public class OrderItemExportChunkFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public OrderItemExportChunk create(OrderItemExportBO exportBO) {
        OrderItemExportChunk finAllowanceItemExportChunk = new OrderItemExportChunk(exportBO);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(finAllowanceItemExportChunk);
        return finAllowanceItemExportChunk;
    }

}