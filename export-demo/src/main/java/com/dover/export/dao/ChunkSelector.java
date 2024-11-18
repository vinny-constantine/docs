package com.dover.export.dao;

import com.dover.export.entity.BasePO;

import java.util.List;

/**
 * @author dover
 * @since 2020/11/17
 */
public interface ChunkSelector {

    /**
     * 查询块，返回结果必须按主键升序
     *
     * @param Chunk chunk
     * @param <PK>  数据主键类型
     * @param <T>   数据类型
     * @return List<T>
     */
    <PK extends Number, T extends BasePO, M extends ChunkSelector> List<T> selectChunk(Chunk<PK, T, M> Chunk);
}
