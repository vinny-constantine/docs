
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
@Slf4j
public final class ExcelUtil {
    //导出临时目录路径
    public static final String DIR_PATH = "tmp_export";
    //导出excel的content-type值
    public static final String CONTENT_TYPE_EXCEL_VALUE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";


    @SneakyThrows
    public static void main(String[] args) {
        export(Files.newOutputStream(Paths.get("D:/tmp.xlsx")), DemoData.class, queryData(RandomUtils.nextInt(5)));
    }

    /**
     * 分页查询数据并导出
     * @param outputStream 输出流
     * @param clazz 导出数据类
     * @param commonPageReq 分页查询参数
     * @param queryDataFunction 分页查询方法
     * @param <T> 导出数据类型
     * @param <R> 导出入参数据类型
     */
    public static <T, R extends CommonPageReq> void export(OutputStream outputStream, Class<T> clazz, R commonPageReq, Function<R, CommonPageResult<T>> queryDataFunction) {
        File tempFile = null;
        try {
            // 创建临时文件
            Path dir = Paths.get(DIR_PATH);
            if (!Files.exists(dir)) Files.createDirectory(dir);
            tempFile = Files.createTempFile(dir, "tmp", ".xlsx").toFile();
            // 创建样式策略
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(getHeaderCellStyle(IndexedColors.BLACK), getBodyCellStyle(IndexedColors.BLACK));
            // 创建 ExcelWriterBuilder
            ExcelWriterBuilder writerBuilder = EasyExcel.write(tempFile, clazz).registerWriteHandler(horizontalCellStyleStrategy);

            // 创建 ExcelWriter
            ExcelWriter excelWriter = writerBuilder.build();
            // 创建sheet
            WriteSheet writeSheet = writerBuilder.sheet("Sheet1").build();
            // 查询一次数据
            commonPageReq.setPageNum(Optional.ofNullable(commonPageReq.getPageNum()).orElse(1));
            commonPageReq.setPageSize(Optional.ofNullable(commonPageReq.getPageSize()).orElse(1000));
            CommonPageResult<T> pageResult = queryDataFunction.apply(commonPageReq);
            CommonPage<T> pageData = pageResult.getData();
            if(pageResult.isSuccess() && pageData != null && CollUtil.isNotEmpty(pageData.getList())) {
                // 写入数据
                excelWriter.write(pageData.getList(), writeSheet);
                // 判断是否还剩余数据
                while (pageData.getPages() > 1 && commonPageReq.getPageNum() < pageData.getPages()) {
                    commonPageReq.setPageNum(commonPageReq.getPageNum() + 1);
                    CommonPageResult<T> nextPageResult = queryDataFunction.apply(commonPageReq);
                    if(nextPageResult.isSuccess() && nextPageResult.getData() != null && CollUtil.isNotEmpty(nextPageResult.getData().getList())) {
                        //写入该页数据
                        excelWriter.write(nextPageResult.getData().getList(), writeSheet);
                        //更新页码
                        pageData = nextPageResult.getData();
                    } else {
                        break;
                    }
                }
            } else {
                excelWriter.write(new ArrayList<>(), writeSheet);
            }
            excelWriter.finish();
            IOUtils.copy(Files.newInputStream(tempFile.toPath()), outputStream);
        } catch (IOException e) {
            log.error("导出失败", e);
        } finally {
            if (tempFile != null && !tempFile.delete()) {
                log.warn("临时文件删除失败，文件路径：{}", tempFile.getAbsolutePath());
            }
        }
    }
    /**
     * 一次性导出所有数据
     * @param outputStream 输出流
     * @param clazz 导出数据类
     * @param dataList 数据
     * @param <T> 导出数据类型
     */
    public static <T> void export(OutputStream outputStream, Class<T> clazz, List<T> dataList) {
        File tempFile = null;
        try {
            // 创建临时文件
            Path dir = Paths.get(DIR_PATH);
            if (!Files.exists(dir)) Files.createDirectory(dir);
            tempFile = Files.createTempFile(dir, "tmp", ".xlsx").toFile();
            // 创建样式策略
            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(getHeaderCellStyle(IndexedColors.BLACK), getBodyCellStyle(IndexedColors.BLACK));
            // 创建 ExcelWriterBuilder
            ExcelWriterBuilder writerBuilder = EasyExcel.write(tempFile, clazz)
                .registerWriteHandler(horizontalCellStyleStrategy);
            // 创建 ExcelWriter
            ExcelWriter excelWriter = writerBuilder.build();
            // 写入数据
            WriteSheet writeSheet = writerBuilder.sheet("Sheet1").build();
            excelWriter.write(dataList, writeSheet);
            excelWriter.finish();
            IOUtils.copy(Files.newInputStream(tempFile.toPath()), outputStream);
        } catch (IOException e) {
            log.error("导出失败", e);
        } finally {
            if (tempFile != null && !tempFile.delete()) {
                log.warn("临时文件删除失败，文件路径：{}", tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * 获取表头字体
     */
    public static WriteCellStyle getHeaderCellStyle(IndexedColors fontColor) {
        // 创建字体，设置为红色
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setColor(fontColor.getIndex());
        headWriteFont.setFontHeightInPoints((short) 14);
        headWriteFont.setBold(true);
        headWriteFont.setFontName("宋体");
        // 定义表头样式
        WriteCellStyle cellStyle = new WriteCellStyle();
        cellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWriteFont(headWriteFont);
        // 设置水平居中
        cellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 设置垂直居中为居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 背景灰色
        cellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 设置单元格上下左右边框为细边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setWrapped(true);
        return cellStyle;
    }
    /**
     * 获取表体字体
     */
    public static WriteCellStyle getBodyCellStyle(IndexedColors fontColor) {
        // 创建字体，设置为红色
        WriteFont writeFont = new WriteFont();
        writeFont.setColor(fontColor.getIndex());
        writeFont.setFontHeightInPoints((short) 10);
        writeFont.setFontName("宋体");
        // 定义表头样式
        WriteCellStyle cellStyle = new WriteCellStyle();
        cellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWriteFont(writeFont);
        // 设置水平居中
        cellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 背景灰色
//        cellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
//        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 设置单元格上下左右边框为细边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setWrapped(true);
        return cellStyle;
    }
    // 模拟查询数据的方法
    private static List<DemoData> queryData(int queryIndex) {
        List<DemoData> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            data.add(new DemoData("Name---------------------" + (queryIndex * 5 + i), (queryIndex * 5 + i)));
        }
        return data;
    }

    // 定义数据实体类
    @Data
    @AllArgsConstructor
    static class DemoData {
        private String name;
        private Integer age;
    }
}