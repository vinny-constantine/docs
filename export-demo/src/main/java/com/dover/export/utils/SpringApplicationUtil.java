package com.dover.export.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

public class SpringApplicationUtil {

    private static ApplicationContext APPLICATION_CONTEXT;

    public static <T> T getBean(Class<T> clazz) {
        return APPLICATION_CONTEXT.getBean(clazz);
    }

    public static <T> T getBean(String name) {
        return (T) APPLICATION_CONTEXT.getBean(name);
    }


    @Configuration
    public static class ApplicationContextMonitor implements ApplicationContextAware {
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            APPLICATION_CONTEXT = applicationContext;
        }
    }

    public static void main(String[] args) {
        int[] a = {0, 3245, 5951, 5520356, 5520349, 5520347, 5520346, 5520343, 5520342, 5520341, 5520340, 979, 565, 564, 568, 567, 566, 505, 509, 972, 508, 512, 507, 511, 506, 510, 416, 420, 415, 419, 414, 418, 417, 7};
        String sql = "SELECT COUNT(1) FROM store_stock WHERE enabled = 1 AND location = ? AND warehouse_id = ? AND store_id = ? AND commodity_sku_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AND commodity_category_id IN (?) AND commodity_amount > 0";
        for (int i : a) {
            sql = sql.replaceFirst("\\?", i + "");
        }
        System.out.println(sql);
    }
}
