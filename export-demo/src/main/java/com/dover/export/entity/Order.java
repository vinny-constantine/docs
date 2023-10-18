package com.dover.export.entity;

import com.dover.export.annotation.ExcelColumn;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * order
 *
 * @author
 */
@Data
@SuperBuilder
public class Order extends BasePO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelColumn(value = "订单编号", col = 0)
    private Integer orderNumber;
    @ExcelColumn(value = "下单日期", col = 1)
    private LocalDate orderDate;
    @ExcelColumn(value = "合约日期", col = 2)
    private LocalDate requiredDate;
    @ExcelColumn(value = "交付日期", col = 3)
    private LocalDate shippedDate;
    @ExcelColumn(value = "状态", col = 4)
    private String status;
    @ExcelColumn(value = "备注", col = 5)
    private String comments;
    @ExcelColumn(value = "客户编号", col = 6)
    private Integer customerNumber;
}