package com.dover.export.dao;

import com.dover.export.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderDao extends ChunkSelector {
    int deleteByPrimaryKey(Integer orderNumber);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer orderNumber);

    List<Order> selectByOrderDate(LocalDate date);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
}