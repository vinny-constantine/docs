package com.dover.export.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * order_items
 * @author 
 */
@Data
@SuperBuilder
public class OrderItemExportBO extends OrderItem{


    //*********************** 导出参数 ***********************//
    /**
     * 每页导出大小
     */
    private Integer size;

    //*********************** 入参条件 ***********************//

}