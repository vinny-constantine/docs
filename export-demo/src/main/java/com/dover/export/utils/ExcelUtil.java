package com.dover.export.utils;

import com.dover.export.annotation.ExcelColumn;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * dover
 */
public class ExcelUtil {

    private final static Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    private final static String EXCEL2003 = "xls";
    private final static String EXCEL2007 = "xlsx";
    private final static String EXCEL_XLS = ".xls";
    private final static String EXCEL_XLSX = ".xlsx";
    /**
     * field cache
     */
    private static final ConcurrentHashMap<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<>();


    public static <T> List<T> readExcel(String path, Class<T> cls, MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            log.error("上传文件格式不正确");
        }
        List<T> dataList = new ArrayList<>();
        Workbook workbook = null;
        try {
            InputStream is = file.getInputStream();
            if (fileName.endsWith(EXCEL2007)) {
//                FileInputStream is = new FileInputStream(new File(path));
                workbook = new XSSFWorkbook(is);
            }
            if (fileName.endsWith(EXCEL2003)) {
//                FileInputStream is = new FileInputStream(new File(path));
                workbook = new HSSFWorkbook(is);
            }
            if (workbook != null) {
                //类映射  注解 value-->bean columns
                Map<String, List<Field>> classMap = new HashMap<>();
                List<Field> fields = Stream.of(cls.getDeclaredFields()).collect(Collectors.toList());
                fields.forEach(field -> {
                    ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                    if (annotation != null) {
                        String value = annotation.value();
                        if (StringUtils.isBlank(value)) {
                            return;//return起到的作用和continue是相同的 语法
                        }
                        if (!classMap.containsKey(value)) {
                            classMap.put(value, new ArrayList<>());
                        }
                        field.setAccessible(true);
                        classMap.get(value).add(field);
                    }
                });
                //索引-->columns
                Map<Integer, List<Field>> reflectionMap = new HashMap<>(16);
                //默认读取第一个sheet
                Sheet sheet = workbook.getSheetAt(0);

                boolean firstRow = true;
                for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    //首行  提取注解
                    if (firstRow) {
                        for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j);
                            String cellValue = getCellValue(cell);
                            if (classMap.containsKey(cellValue)) {
                                reflectionMap.put(j, classMap.get(cellValue));
                            }
                        }
                        firstRow = false;
                    } else {
                        //忽略空白行
                        if (row == null) {
                            continue;
                        }
                        try {
                            T t = cls.newInstance();
                            //判断是否为空白行
                            boolean allBlank = true;
                            for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
                                if (reflectionMap.containsKey(j)) {
                                    Cell cell = row.getCell(j);
                                    String cellValue = getCellValue(cell);
                                    if (StringUtils.isNotBlank(cellValue)) {
                                        allBlank = false;
                                    }
                                    List<Field> fieldList = reflectionMap.get(j);
                                    fieldList.forEach(x -> {
                                        try {
                                            handleField(t, cellValue, x);
                                        } catch (Exception e) {
                                            log.error(String.format("reflect field:%s value:%s exception!", x.getName(), cellValue), e);
                                        }
                                    });
                                }
                            }
                            if (!allBlank) {
                                dataList.add(t);
                            } else {
                                log.warn(String.format("row:%s is blank ignore!", i));
                            }
                        } catch (Exception e) {
                            log.error(String.format("parse row:%s exception!", i), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("parse excel exception!", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("parse excel exception!", e);
                }
            }
        }
        return dataList;
    }

    public static <T> void handleField(T t, String value, Field field) throws Exception {
        Class<?> type = field.getType();
        if (type == null || type == void.class || StringUtils.isBlank(value)) {
            return;
        }
        if (type == Object.class) {
            field.set(t, value);
            //数字类型
        } else if (type.getSuperclass() == null || type.getSuperclass() == Number.class) {
            if (type == int.class || type == Integer.class) {
                double aDouble = Double.parseDouble(value);
                field.set(t, (int) aDouble);
                field.set(t, NumberUtils.toInt(value));
            } else if (type == long.class || type == Long.class) {
                field.set(t, NumberUtils.toLong(value));
            } else if (type == byte.class || type == Byte.class) {
                field.set(t, NumberUtils.toByte(value));
            } else if (type == short.class || type == Short.class) {
                field.set(t, NumberUtils.toShort(value));
            } else if (type == double.class || type == Double.class) {
                field.set(t, NumberUtils.toDouble(value));
            } else if (type == float.class || type == Float.class) {
                field.set(t, NumberUtils.toFloat(value));
            } else if (type == char.class || type == Character.class) {
                field.set(t, CharUtils.toChar(value));
            } else if (type == boolean.class) {
                field.set(t, BooleanUtils.toBoolean(value));
            } else if (type == BigDecimal.class) {
                field.set(t, new BigDecimal(value));
            }
        } else if (type == Boolean.class) {
            field.set(t, BooleanUtils.toBoolean(value));
        } else if (type == Date.class) {
            //
            field.set(t, value);
        } else if (type == String.class) {
            field.set(t, value);
        } else {
            Constructor<?> constructor = type.getConstructor(String.class);
            field.set(t, constructor.newInstance(value));
        }
    }

    /**
     * 获取单元格值
     *
     * @param cell 单元格
     * @return String
     */
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).toString();
            } else {
//                cell.setCellType(1);
                return cell.getStringCellValue();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            return StringUtils.trimToEmpty(cell.getStringCellValue());
        } else if (cell.getCellType() == CellType.FORMULA) {
            return StringUtils.trimToEmpty(cell.getCellFormula());
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.ERROR) {
            return "ERROR";
        } else {
            return cell.toString().trim();
        }

    }

    /**
     * 写excel 文件
     *
     * @param fileName 文件名称
     * @param response response
     * @param dataList 实体集合
     * @param cls      实体类型class
     * @param <T>      实体
     */
    public static <T> void writeExcel(String fileName, HttpServletResponse response, List<T> dataList, Class<T> cls) {
        // 加载字段列表
        List<Field> fieldList = loadFields(cls);

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");

        //设置单元格格式 文本
        DataFormat dataFormat = wb.createDataFormat();
        CellStyle columnStyle = wb.createCellStyle();
        columnStyle.setDataFormat(dataFormat.getFormat("@"));
        for (int i = 0; i < fieldList.size(); i++) {
            sheet.setDefaultColumnStyle(i, columnStyle);
        }

        // 行号
        AtomicInteger rowCount = new AtomicInteger();

        //写入头部
        AtomicInteger headerColumnCount = new AtomicInteger();
        Row headerRow = sheet.createRow(rowCount.getAndIncrement());
        fieldList.forEach(field -> {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            String columnName = Optional.ofNullable(annotation).map(ExcelColumn::value).orElse("");

            Cell cell = headerRow.createCell(headerColumnCount.getAndIncrement());
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN); //下边框
//            cellStyle.setBottomBorderColor(HSSFColor.GREY_25_PERCENT.index);//设置边框浅灰色
//            cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
//            cellStyle.setLeftBorderColor(HSSFColor.GREY_25_PERCENT.index);
//            cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
//            cellStyle.setTopBorderColor(HSSFColor.GREY_25_PERCENT.index);
//            cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
//            cellStyle.setRightBorderColor(HSSFColor.GREY_25_PERCENT.index);
//            cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//            cellStyle.setDataFormat(dataFormat.getFormat("@"));
//            Font font = wb.createFont();
//            font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
//            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(columnName);
        });

        if (CollectionUtils.isNotEmpty(dataList)) {
            for (T t : dataList) {
                Row sheetRow = sheet.createRow(rowCount.getAndIncrement());
                AtomicInteger columnCount = new AtomicInteger();
                fieldList.forEach(field -> {
                    Object value = "";
                    try {
                        value = field.get(t);
                    } catch (Exception e) {
                        log.error("getting field value failed", e);
                    }
                    Cell cell = sheetRow.createCell(columnCount.getAndIncrement());
                    if (value != null) {
                        cell.setCellValue(value.toString());
//                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    }
                });
            }
        }

        //冻结窗格

        wb.getSheet("Sheet1").createFreezePane(0, 1, 0, 1);
        //浏览器下载excel

        buildExcelDocument(fileName, wb, response);
        //生成excel文件
        // buildExcelFile(".\\default.xlsx",wb);
    }

    /**
     * 浏览器下载excel
     *
     * @param fileName 文件名称
     * @param wb       workbook
     * @param response response
     */

    public static void buildExcelDocument(String fileName, Workbook wb, HttpServletResponse response) {
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("File-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("File-Name", URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Content-Disposition", "attachment;filename=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"");
            response.flushBuffer();
            wb.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成excel文件
     *
     * @param path 生成excel路径
     * @param wb   workbook
     */
    private static void buildExcelFile(String path, Workbook wb) {

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            wb.write(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param clazz 类对象
     * @param <T>   泛型
     * @return 字段列表
     */
    public static <T> List<Field> loadFields(Class<T> clazz) {
        // 将待导出的实体字段集合缓存起来
        List<Field> fieldList = fieldsCache.get(clazz);
        if (fieldList == null) {
            synchronized (ExcelUtil.class) {
                if ((fieldList = fieldsCache.get(clazz)) == null) {
                    fieldList = Arrays.stream(clazz.getDeclaredFields()).filter(field -> {
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        if (annotation != null && annotation.col() > 0) {
                            field.setAccessible(true);
                            return true;
                        }
                        return false;
                    }).sorted(Comparator.comparing(field -> {
                        int col = 0;
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        if (annotation != null) {
                            col = annotation.col();
                        }
                        return col;
                    })).collect(Collectors.toList());
                    fieldsCache.put(clazz, fieldList);
                }
            }
        }
        return fieldList;
    }

    /**
     * find field by annotation
     *
     * @param annotationClass annotation class
     * @param targetClass     target class
     * @param <T>             annotation type
     * @param <K>             target type
     * @return field
     */
    public static <T extends Annotation, K> Field findField(Class<T> annotationClass, Class<K> targetClass) {
        for (Field field : loadFields(targetClass)) {
            if (field.getAnnotation(annotationClass) != null) {
                return field;
            }
        }
        return null;
    }

    /**
     * 写入头部
     *
     * @param sheet     表
     * @param cellStyle 头部单元格样式
     * @param fields    字段列表
     */
    public static void writeHeader(Sheet sheet, CellStyle cellStyle, List<Field> fields) {
        AtomicInteger headerColumnCount = new AtomicInteger();
        Row headerRow = sheet.createRow(0);
        fields.forEach(field -> {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            String columnName = Optional.ofNullable(annotation).map(ExcelColumn::value).orElse("");
            Cell cell = headerRow.createCell(headerColumnCount.getAndIncrement());
            cell.setCellStyle(cellStyle);
            cell.setCellValue(columnName);
        });
    }

    /**
     * 创建excel字段行,并进行数据填充
     *
     * @param colNameList 字段行的列名列表
     * @param exports     导出数据
     * @param sheet       {@link SXSSFSheet}
     * @param startRowNum 起始行号
     * @param biConsumer  数据填充的函数
     * @param <T>         导出数据类型
     */
    public static <T> void createFiledRowAndAssemble(List<String> colNameList, List<T> exports, SXSSFSheet sheet, Integer startRowNum, BiConsumer<List<T>, Integer> biConsumer) {
        Row filedRow = sheet.createRow(startRowNum);
        for (int i = 0; i < colNameList.size(); i++) {
            filedRow.createCell(i).setCellValue(colNameList.get(i));
        }

        if (CollectionUtils.isNotEmpty(exports)) {
            biConsumer.accept(exports, startRowNum);
        }
    }

    /**
     * 读取excel
     *
     * @param filePath filePath
     * @return Workbook
     */
    public static Workbook readExcel(String filePath) {
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is;
        try {
            is = new FileInputStream(filePath);
            if (EXCEL_XLS.equals(extString)) {
                return new HSSFWorkbook(is);
            } else if (EXCEL_XLSX.equals(extString)) {
                return new XSSFWorkbook(is);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
