package com.dover.export.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author dover
 * @since 2023/10/18
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePO {
    private static final long serialVersionUID = 1298022558008860710L;
    /**
     * 主键ID
     */
    private Long id;
}
