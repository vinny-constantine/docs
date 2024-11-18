package com.dover.export.dao;

import com.dover.export.annotation.ExcelColumn;
import com.dover.export.entity.BasePO;
import com.dover.export.utils.ExcelStreamingUtil;
import com.dover.export.utils.SpringApplicationUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 该类线程不安全
 *
 * @author dover
 * @since 2020/11/16
 */
@SuppressWarnings("all")
public abstract class Chunk<PK extends Number, T extends BasePO, M extends ChunkSelector> {

    /**
     * 默认主键
     */
    public static final String DEFAULT_PRIMARY_KEY = "id";

    /**
     * 上次查询最大ID
     */
    protected volatile Number lastMaxId;

    /**
     * 块大小
     */
    protected Integer size;

    /**
     * 查询条件
     */
    protected T condition;

    /**
     * 当前主键值下确界拼接sql
     */
    private volatile String lowerBoundSQL;

    /**
     * 当前数据
     */
    protected volatile List<T> data;

    /**
     * 数据的实体类型
     */
    private Class<T> dataClass;

    /**
     * 主键字段
     */
    private String primaryKeyName;

    /**
     * 主键在实体类型中的属性
     */
    private Field primaryKeyField;

    /**
     * mapper
     */
    private ChunkSelector chunkSelector;


    public Chunk(Integer size, T condition) {
        Type[] typeArguments = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
        this.lastMaxId = 0;
        this.size = size;
        this.condition = condition;
        this.dataClass = (Class<T>) typeArguments[1];
        this.chunkSelector = SpringApplicationUtil.getBean((Class<M>) typeArguments[2]);
        this.primaryKeyField = Optional.ofNullable(ExcelStreamingUtil.findField(anno -> anno.isKey(), dataClass))
            .orElse(ReflectionUtils.findField(dataClass, DEFAULT_PRIMARY_KEY));
        this.primaryKeyField.setAccessible(true);
        this.primaryKeyName = Optional.ofNullable(this.primaryKeyField)
            .map(x -> Optional.ofNullable(x.getAnnotation(ExcelColumn.class))
                .map(y -> "".equals(y.columnName()) ? Arrays.stream(
                        StringUtils.splitByCharacterTypeCamelCase(this.primaryKeyField.getName()))
                    .map(String::toLowerCase)
                    .collect(Collectors.joining("_")) : y.columnName())
                .orElse(DEFAULT_PRIMARY_KEY))
            .orElse(DEFAULT_PRIMARY_KEY);
    }

    public Class<T> getDataClass() {
        return dataClass;
    }

    /**
     * 最先被调用
     */
    @SneakyThrows
    public boolean hasNext() {
        this.data = chunkSelector.selectChunk(this);
        if (CollectionUtils.isEmpty(data)) {
            return false;
        }
        this.lastMaxId = (Number) this.primaryKeyField.get(data.get(data.size() - 1));
        this.lowerBoundSQL = String.format("%s > %s limit %s", this.primaryKeyName, this.lastMaxId, this.size);
        return true;
    }

    /**
     * 其次被调用
     */
    public List<T> next() {
        List<T> tmp = this.data;
        this.data = null;
        return tmp;
    }

    /**
     * 最后被调用
     * 将列值映射为导出值
     */
    public String map(Integer columnIdx, String columnName, Object value, Map<String, Object> rowData) {
        return Optional.ofNullable(value).map(Object::toString).orElse("");
    }
}

