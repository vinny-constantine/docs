package com.dover.export.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * order_items
 * @author 
 */
@Data
@SuperBuilder
public class OrderItem extends BasePO implements Serializable {
    private Long id;

    private Integer orderNumber;

    private String productCode;

    private Integer quantityOrdered;

    private BigDecimal priceEach;

    private Short orderLineNumber;

    private static final long serialVersionUID = 1L;
}