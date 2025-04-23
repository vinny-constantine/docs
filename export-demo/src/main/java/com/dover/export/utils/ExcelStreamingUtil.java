package com.dover.export.utils;


import com.dover.export.annotation.ExcelColumn;
import com.dover.export.dao.Chunk;
import com.dover.export.dao.ChunkSelector;
import com.dover.export.entity.BasePO;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * based on big-grid demo
 *
 * @author dover
 * @since 2020/11/25
 */
@Log
public class ExcelStreamingUtil {

    /**
     * 模板文件名
     */
    private static final String TEMPLATE = System.getProperty("user.home") + "/template.xlsx";
    /**
     * field cache
     */
    private static ConcurrentHashMap<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<>();


    public static void main(String[] args) throws Exception {
        // Step 1. Create a template file. Setup sheets and workbook-level objects such as
        // cell styles, number formats, etc.
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Big Grid");
        Map<StyleEnum, CellStyle> styles = createStyles(wb);
        //name of the zip entry holding sheet data, e.g. /xl/worksheets/sheet1.xml
        String sheetRef = sheet.getPackagePart().getPartName().getName();
        //save the template
        FileOutputStream os = new FileOutputStream(TEMPLATE);
        wb.write(os);
        os.close();
        //Step 2. Generate XML file.
        File tmp = File.createTempFile("sheet", ".xml");
        Writer fw = new FileWriter(tmp);
        generate(fw, styles);
        fw.close();
        //Step 3. Substitute the template entry with the generated data
        FileOutputStream out = new FileOutputStream("big-grid.xlsx");
        substitute(new File(TEMPLATE), tmp, sheetRef.substring(1), out);
        out.close();
    }

    private static void generate(Writer out, Map<StyleEnum, CellStyle> styles) throws Exception {
        Random rnd = new Random();
        Calendar calendar = Calendar.getInstance();
        SpreadsheetWriter writer = new SpreadsheetWriter(out);
        writer.beginSheet();
        //insert header row
        writer.insertRow(0);
        int styleIndex = styles.get(StyleEnum.HEADER).getIndex();
        writer.createCell(0, "Title", styleIndex);
        writer.createCell(1, "% Change", styleIndex);
        writer.createCell(2, "Ratio", styleIndex);
        writer.createCell(3, "Expenses", styleIndex);
        writer.createCell(4, "Date", styleIndex);
        writer.endRow();
        //write data rows
        for (int rownum = 1; rownum < 100000; rownum++) {
            writer.insertRow(rownum);
            writer.createCell(0, "Hello, " + rownum + "!");
            writer.createCell(1, (double) rnd.nextInt(100) / 100, styles.get(StyleEnum.PERCENT).getIndex());
            writer.createCell(2, (double) rnd.nextInt(10) / 10, styles.get(StyleEnum.COEFFICIENT).getIndex());
            writer.createCell(3, rnd.nextInt(10000), styles.get(StyleEnum.CURRENCY).getIndex());
            writer.createCell(4, calendar, styles.get(StyleEnum.DATE).getIndex());
            writer.endRow();
            calendar.roll(Calendar.DAY_OF_YEAR, 1);
        }
        writer.endSheet();
    }

    /**
     * 根据 sxssf 导出
     *
     * @param outputStream 导出流
     * @param chunk        数据块
     * @param <PK>         主键类型
     * @param <T>          实体类型
     * @param <M>          实体对应Mapper
     */
    public static <PK extends Number, T extends BasePO, M extends ChunkSelector> void exportBySXSSF(
        OutputStream outputStream, Chunk<PK, T, M> chunk) {
        SXSSFWorkbook wb = new SXSSFWorkbook(-1);
        Map<StyleEnum, CellStyle> styleMap = createStyles(wb);
        try {
            SXSSFSheet sheet = wb.createSheet();
            List<Field> fields = loadFields(chunk.getDataClass());
            // header
            int rowCount = 0;
            int headerColumnCount = 0;
            Row headerRow = sheet.createRow(rowCount++);
            for (Field field : fields) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                if (!annotation.show()) continue;
                String columnName = Optional.ofNullable(annotation).map(ExcelColumn::value).orElse("");
                Cell cell = headerRow.createCell(headerColumnCount++);
                cell.setCellStyle(styleMap.get(StyleEnum.HEADER));
                cell.setCellValue(columnName);
            }
            // data
            while (chunk.hasNext()) {
                for (T t : chunk.next()) {
                    int columnCount = 0;
                    Row row = sheet.createRow(rowCount++);
                    Map<String, Object> rowData = new LinkedHashMap<>();
                    for (Field field : fields) {
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        if (!annotation.show()) continue;
                        rowData.put(field.getName(), field.get(t));
                    }
                    for (String key : rowData.keySet()) {
                        String columnValue = chunk.map(columnCount, key, rowData.get(key), rowData);
                        Cell cell = row.createCell(columnCount++);
                        cell.setCellStyle(styleMap.get(StyleEnum.NORMAL));
                        cell.setCellValue(columnValue);
                    }
                }
                // manually control how rows are flushed to disk
                sheet.flushRows();
            }
            wb.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // dispose of temporary files backing this workbook on disk
            wb.dispose();
        }
    }


    /**
     * streaming export
     *
     * @param outputStream output target
     * @param chunk        data chunk
     */
    public static <PK extends Number, T extends BasePO, M extends ChunkSelector> void export(
        OutputStream outputStream, Chunk<PK, T, M> chunk) {
        // 1. create workbook
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet1");
        try (FileOutputStream output = new FileOutputStream(TEMPLATE)) {
            // 2. save the template with style
            Map<StyleEnum, CellStyle> styles = createStyles(wb);
            wb.write(output);
            output.close();
            // 3. generate the data carrier
            File tmp = File.createTempFile("sheet", ".xml");
            FileWriter writer = new FileWriter(tmp);
            // 4. fill data
            Painter painter = new Painter(new SpreadsheetWriter(writer), styles);
            doWrite(chunk, painter);
            writer.close();
            // 5. transfer data
            substitute(new File(TEMPLATE), tmp, sheet.getPackagePart().getPartName().getName().substring(1),
                outputStream);
        } catch (Exception e) {
            throw new RuntimeException("error when generating excel", e);
        }
    }

    /**
     * write excel
     *
     * @param chunk   data container
     * @param painter writer and styles
     */
    public static <PK extends Number, T extends BasePO, M extends ChunkSelector> void doWrite(Chunk<PK, T, M> chunk,
        Painter painter) {
        try {
            ExcelStreamingUtil.SpreadsheetWriter writer = painter.getWriter();
            Map<StyleEnum, CellStyle> styles = painter.getStyles();
            List<Field> fields = loadFields(chunk.getDataClass());
            writer.beginSheet();
            // header
            int rowCount = 0;
            int headerColumnCount = 0;
            writer.insertRow(rowCount++);
            short index = styles.get(StyleEnum.HEADER).getIndex();
            for (Field field : fields) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                if (!annotation.show()) continue;
                String columnName = Optional.ofNullable(annotation).map(ExcelColumn::value).orElse("");
                writer.createCell(headerColumnCount++, columnName, index);
            }
            writer.endRow();
            // data
            while (chunk.hasNext()) {
                for (T t : chunk.next()) {
                    int columnCount = 0;
                    writer.insertRow(rowCount++);
                    LinkedHashMap<String, Object> rowData = new LinkedHashMap<>();
                    for (Field field : fields) {
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        if (!annotation.show()) continue;
                        rowData.put(field.getName(), field.get(t));
                    }
                    for (String key : rowData.keySet()) {
                        String columnValue = chunk.map(columnCount, key, rowData.get(key), rowData);
                        writer.createCell(columnCount++, columnValue, styles.get(StyleEnum.NORMAL).getIndex());
                    }
                    writer.endRow();
                }
                writer.flush();
            }
            writer.endSheet();
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("generate excel error", e);
        }
    }

    /**
     * 创建样式
     *
     * @param wb
     * @return
     */
    private static Map<StyleEnum, CellStyle> createStyles(Workbook wb) {
        Map<StyleEnum, CellStyle> styleMap = new HashMap<>();
        DataFormat fmt = wb.createDataFormat();
        CellStyle normalStyle = wb.createCellStyle();
        normalStyle.setWrapText(true);
        normalStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        normalStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        styleMap.put(StyleEnum.NORMAL, normalStyle);
        CellStyle percentStyle = wb.createCellStyle();
        percentStyle.setDataFormat(fmt.getFormat("0.0%"));
        percentStyle.setWrapText(true);
        percentStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        percentStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        styleMap.put(StyleEnum.PERCENT, percentStyle);
        CellStyle coefficientStyle = wb.createCellStyle();
        coefficientStyle.setDataFormat(fmt.getFormat("0.0X"));
        coefficientStyle.setWrapText(true);
        coefficientStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        coefficientStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        styleMap.put(StyleEnum.COEFFICIENT, coefficientStyle);
        CellStyle currencyStyle = wb.createCellStyle();
        currencyStyle.setDataFormat(fmt.getFormat("￥#,##0.00"));
        currencyStyle.setWrapText(true);
        currencyStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        currencyStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        styleMap.put(StyleEnum.CURRENCY, currencyStyle);
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(fmt.getFormat("yyyy-MM-dd"));
        dateStyle.setWrapText(true);
        dateStyle.setAlignment(HorizontalAlignment.JUSTIFY);
        dateStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        styleMap.put(StyleEnum.DATE, dateStyle);
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styleMap.put(StyleEnum.HEADER, headerStyle);
        return styleMap;
    }

    /**
     * @param zipFile the template file
     * @param tmpFile the XML file with the sheet data
     * @param entry   the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
     * @param out     the stream to write the result
     */
    private static void substitute(File zipFile, File tmpFile, String entry, OutputStream out) throws IOException {
        try (ZipFile templateZip = new ZipFile(zipFile); //
             InputStream is = new FileInputStream(tmpFile); //
             ZipOutputStream zos = new ZipOutputStream(out)) {
            Enumeration<? extends ZipEntry> entries = templateZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                // copy the nested entry except sheet
                if (!zipEntry.getName().equals(entry)) {
                    try (InputStream inputStream = templateZip.getInputStream(zipEntry)) {
                        zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                        copyStream(inputStream, zos);
                        zos.flush();
                    }
                }
            }
            // copy the sheet
            zos.putNextEntry(new ZipEntry(entry));
            copyStream(is, zos);
            zos.close();
            // delete the template
            if (zipFile.exists()) {
                zipFile.delete();
            }
        }
    }


    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >= 0) {
            out.write(chunk, 0, count);
        }
    }

    /**
     * Writes spreadsheet data in a Writer.
     * (YK: in future it may evolve in a full-featured API for streaming data in Excel)
     */
    public static class SpreadsheetWriter {
        private final Writer _out;
        private int _rownum;

        public SpreadsheetWriter(Writer out) {
            _out = out;
        }


        @SneakyThrows
        public void flush() {
            _out.flush();
        }

        @SneakyThrows
        public void close() {
            _out.close();
        }

        public void beginSheet() throws IOException {
            _out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
            _out.write("<sheetData>\n");
        }

        public void endSheet() throws IOException {
            _out.write("</sheetData>");
            _out.write("</worksheet>");
        }

        /**
         * Insert a new row
         *
         * @param rownum 0-based row number
         */
        public void insertRow(int rownum) throws IOException {
            _out.write("<row r=\"" + (rownum + 1) + "\">\n");
            this._rownum = rownum;
        }

        /**
         * Insert row end marker
         */
        public void endRow() throws IOException {
            _out.write("</row>\n");
        }

        public void createCell(int columnIndex, String value, int styleIndex) throws IOException {
            String ref = new CellReference(_rownum, columnIndex).formatAsString();
            _out.write("<c r=\"" + ref + "\" t=\"inlineStr\"");
            if (styleIndex != -1) {
                _out.write(" s=\"" + styleIndex + "\"");
            }
            _out.write(">");
            _out.write("<is><t>" + value + "</t></is>");
            _out.write("</c>");
        }

        public void createCell(int columnIndex, String value) throws IOException {
            createCell(columnIndex, value, -1);
        }

        public void createCell(int columnIndex, double value, int styleIndex) throws IOException {
            String ref = new CellReference(_rownum, columnIndex).formatAsString();
            _out.write("<c r=\"" + ref + "\" t=\"n\"");
            if (styleIndex != -1) {
                _out.write(" s=\"" + styleIndex + "\"");
            }
            _out.write(">");
            _out.write("<v>" + value + "</v>");
            _out.write("</c>");
        }

        public void createCell(int columnIndex, double value) throws IOException {
            createCell(columnIndex, value, -1);
        }

        public void createCell(int columnIndex, Calendar value, int styleIndex) throws IOException {
            createCell(columnIndex, DateUtil.getExcelDate(value, false), styleIndex);
        }
    }

    /**
     * wrap writer and styles
     */
    public static class Painter {
        private SpreadsheetWriter writer;
        private Map<StyleEnum, CellStyle> styles;

        public Painter(SpreadsheetWriter writer, Map<StyleEnum, CellStyle> styles) {
            this.writer = writer;
            this.styles = styles;
        }

        public SpreadsheetWriter getWriter() {
            return writer;
        }

        public Map<StyleEnum, CellStyle> getStyles() {
            return styles;
        }
    }

    /**
     * @author dover
     * @since 2020/11/17
     */
    public enum StyleEnum {
        DATE, PERCENT, NUMERIC, CURRENCY, COEFFICIENT, HEADER, NORMAL,
        ;

        public static StyleEnum match(Class<?> clazz) {
            if (String.class.equals(clazz)) {
                return NORMAL;
            } else if (ClassUtils.isAssignable(BigDecimal.class, clazz)) {
                return CURRENCY;
            } else if (ClassUtils.isAssignable(Number.class, clazz)) {
                return NORMAL;
            } else if (ClassUtils.isAssignable(Date.class, clazz)) {
                return DATE;
            } else {
                return NORMAL;
            }
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
                    fieldList = getDeclaredFieldsRecursive(clazz).stream().filter(field -> {
                        ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                        if (annotation != null && annotation.col() >= 0) {
                            field.setAccessible(true);
                            return true;
                        }
                        return false;
                    }).sorted(Comparator.comparing(field -> {
                        return field.getAnnotation(ExcelColumn.class).col();
                    })).collect(Collectors.toList());
                    fieldsCache.put(clazz, fieldList);
                }
            }
        }
        return fieldList;
    }

    /**
     * 递归查询类及其父类属性
     */
    public static <T> List<Field> getDeclaredFieldsRecursive(Class<T> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            fields.add(declaredField);
        }
        if (clazz.getSuperclass() != Object.class) {
            fields.addAll(getDeclaredFieldsRecursive(clazz.getSuperclass()));
            return fields;
        }
        return fields;
    }

    /**
     * find field by annotation
     *
     * @param <K>         target type
     * @param expression  ExcelColumn expression
     * @param targetClass target class
     * @return field
     */
    public static <K> Field findField(Function<ExcelColumn, Boolean> expression, Class<K> targetClass) {
        for (Field field : loadFields(targetClass)) {
            if (expression.apply(field.getAnnotation(ExcelColumn.class))) {
                return field;
            }
        }
        return null;
    }

}
