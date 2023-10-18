package com.dover.export.dao;

import com.dover.export.entity.Order;

/**
 *
 */
public class OrderChunk extends Chunk<Integer, Order, OrderDao> {

    public OrderChunk(Integer size, Order condition) {
        super(size, condition);
    }
}
