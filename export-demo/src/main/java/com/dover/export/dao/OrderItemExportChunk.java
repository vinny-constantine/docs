package com.dover.export.dao;

import com.dover.export.entity.OrderItemExportBO;

import java.util.List;
import java.util.Map;

/**
 * @author dover
 * @since 2022/4/24
 */
public class OrderItemExportChunk extends Chunk<Long, OrderItemExportBO, OrderItemDao> {


    public OrderItemExportChunk(OrderItemExportBO condition) {
        super(condition.getSize(), condition);
    }


    @Override
    public List<OrderItemExportBO> next() {
//        if (CollectionUtils.isNotEmpty(this.data)) {
//            this.firstOfficeMap = officeManager.findFirstOfficeByOfficeIds(
//                this.data.stream().map(FinAllowanceItemPO::getOfficeId).collect(Collectors.toList()));
//        }
        return super.next();
    }


    @Override
    public String map(Integer columnIdx, String columnName, Object value, Map<String, Object> rowData) {
//        if ("officeId".equals(columnName)) {
//            return Optional.ofNullable(value)
//                .map(x -> firstOfficeMap.get(x))
//                .map(FirstAndParentOfficeInfo::getFirstOfficeName)
//                .orElse("");
//        }
//        if ("type".equals(columnName)) {
//            return Optional.ofNullable(value).map(x -> FinAllowanceTypeEnum.match((int) x).desc).orElse("");
//        }
//        if ("status".equals(columnName)) {
//            return Optional.ofNullable(value).map(x -> FinAllowanceItemStatusEnum.match((int) x).headDesc).orElse("");
//        }
//        if ("planAmount".equals(columnName) && FinAllowanceTypeEnum.CUSTOMER_SERVICE_ALLOWANCE.val.equals(
//            rowData.get("type"))) { // 客诉计划补贴金额展示为 负数
//            return Optional.ofNullable(value).map(x -> ((BigDecimal) x).negate().toString()).orElse("");
//        }
//        if ("actualAmount".equals(columnName) && FinAllowanceTypeEnum.CUSTOMER_SERVICE_ALLOWANCE.val.equals(
//            rowData.get("type"))) { // 客诉实际补贴金额展示为 负数
//            return Optional.ofNullable(value).map(x -> ((BigDecimal) x).negate().toString()).orElse("");
//        }
//        if ("createdTime".equals(columnName)) {
//            return Optional.ofNullable(value).map(x -> DrpDateUtil.stringTime((LocalDateTime) x)).orElse("");
//        }
//        if ("lastModifiedTime".equals(columnName)) {
//            return Optional.ofNullable(value).map(x -> DrpDateUtil.stringTime((LocalDateTime) x)).orElse("");
//        }
        return super.map(columnIdx, columnName, value, rowData);
    }
}
