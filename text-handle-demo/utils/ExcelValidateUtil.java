
@Slf4j
public class ExcelUtil {

    public static void main(String[] args) {
//        export();
//        exportCheckBox();
        checkbox();
    }
    
    /**
     * 获取excel校验器
     */
    private static DataValidation getDataValidation(ImportTemplateDetailBean importTemplateDetailBean, DataValidationHelper dvHelper, int col) {
        DataValidationConstraint dvConstraint;
        if (ImportTemplateEnums.FieldTypeEnum.NUMBER.code.equals(importTemplateDetailBean.getFieldType())) {//数值
            dvConstraint = dvHelper.createNumericConstraint(DataValidationConstraint.ValidationType.INTEGER, DataValidationConstraint.OperatorType.BETWEEN, "0", "99999");
        } else if (ImportTemplateEnums.FieldTypeEnum.DATE.code.equals(importTemplateDetailBean.getFieldType())) {//日期
            dvConstraint = dvHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=Date(1990, 1, 1)", "=Date(9999, 12, 31)", null);
        } else if (ImportTemplateEnums.FieldTypeEnum.TIME.code.equals(importTemplateDetailBean.getFieldType())) {//时间
            dvConstraint = dvHelper.createTimeConstraint(DataValidationConstraint.OperatorType.BETWEEN, "=TIME(0,0,0)", "=TIME(23,59,59)");
        } else if (ImportTemplateEnums.FieldTypeEnum.RADIO.code.equals(importTemplateDetailBean.getFieldType())) {//单选枚举
            String jsonArray = importTemplateDetailBean.getFieldTypeValue();
            try {
                List<String> radioList = JSON.parseArray(jsonArray, String.class);
                dvConstraint = dvHelper.createExplicitListConstraint(radioList.toArray(new String[0]));
            } catch (Exception e) {
                log.info("解析导入模板字段类型值失败，jsonArray={}", jsonArray, e);
                dvConstraint = dvHelper.createExplicitListConstraint(new String[]{});
            }
        } else if (ImportTemplateEnums.FieldTypeEnum.CHECK_BOX.code.equals(importTemplateDetailBean.getFieldType())) {//多选枚举
            String jsonArray = importTemplateDetailBean.getFieldTypeValue();
            try {
                dvConstraint = dvHelper.createExplicitListConstraint(JSON.parseArray(jsonArray, String.class).toArray(new String[0]));
            } catch (Exception e) {
                log.info("解析导入模板字段类型值失败，jsonArray={}", jsonArray, e);
                dvConstraint = dvHelper.createExplicitListConstraint(new String[]{});
            }
        } else {//默认为文本
            dvConstraint = dvHelper.createTextLengthConstraint(DataValidationConstraint.ValidationType.TEXT_LENGTH, "0", "1024");
        }
        DataValidation validation = dvHelper.createValidation(dvConstraint, new CellRangeAddressList(1, 65535, col, col));
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        return validation;
    }

    /**
     * 设置字体红色
     *
     * @param workbook 工作簿
     * @return
     */
    public static CellStyle getRedFontCellStyle(Workbook workbook, IndexedColors fontColor) {
        // 创建字体，设置为红色
        Font font = workbook.createFont();
        font.setColor(fontColor.getIndex());
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setFontName("宋体");
        // 创建单元格样式
        CellStyle cellStyle = workbook.createCellStyle();
        // 设置字体
        cellStyle.setFont(font);
        // 设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 背景灰色
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 设置垂直居中为居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置左右对齐为居中
        cellStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        // 设置单元格上下左右边框为细边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    public static String toExcelCol(int n) {
        // n = 26^0 * Z + 26^1 * (A + 1) + 26^2 * _
        if (n == 0) return "A";
        StringBuilder s = new StringBuilder();
        while (n > 0) {
            int offset = n % 26;
            if (offset == 0) {
                s.insert(0, 'Z');
                n /= 26;
                n -= 1;
            } else {
                char c = (char) ('A' + offset - 1);
                s.insert(0, c);
                n /= 26;
            }
        }
        return s.toString();
    }
}