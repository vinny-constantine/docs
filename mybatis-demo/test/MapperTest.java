package com.dover.demo.mapper;

import com.dover.demo.util.MybatisUtil;
import com.google.common.collect.Lists;
import org.apache.ibatis.session.SqlSession;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MapperTest {

    static TestMapper mapper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SqlSession sqlSession = MybatisUtil.getSqlSession();
        mapper = sqlSession.getMapper(TestMapper.class);
    }


    @Test
    public void testFindOrderById() {
        mapper.findOrderById("2");
    }
