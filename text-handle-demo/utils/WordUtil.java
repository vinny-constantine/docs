package com.dover.pdf.util;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.Cells;
import com.deepoove.poi.data.MergeCellRule;
import com.deepoove.poi.data.MergeCellRule.Grid;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.Tables;
import com.dover.pdf.font.CNFont;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import fr.opensagres.poi.xwpf.converter.core.IXWPFConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.core.io.UrlResource;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dover
 * @since 2023/7/3
 */
public class WordUtil {

    public static final String templateName = "D:\\Users\\80321000\\Desktop\\word.docx";
    public static final String target = "D:\\Users\\80321000\\Desktop\\demo.pdf";

    @SneakyThrows
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("img", Pictures.ofBufferedImage(QrCodeUtil.createQrcode("https://drp-test.wanyol.com/drp-offline-h5/fileDisplay/index.html#/home?id=%s", 150), PictureType.PNG)
            .size(150, 150)
            .create());
        map.put("year", "2023");
        map.put("lv5OfficeName", "江西");
        map.put("monthBegin", "4");
        map.put("monthEnd", "5");
        map.put("fixedAllowanceAmount", String.format("%s", "20,000"));
        map.put("operationAllowanceAmount", String.format("%s", "8,000"));
        map.put("saleAllowanceAmount", String.format("%s", "15,000"));
        map.put("totalAllowanceAmount", String.format("%s", "43,000"));
        RowRenderData header = Rows.of("序号", "一代名称", "门店名称", "门店编码", "客户组名称", "门店等级", "城市等级",
                "补贴周期", "固定支持金额(含税)单位：元", "运营激励金额(含税)单位：元", "销售激励金额(含税)单位：元",
                "补贴费用合计(含税)单位：元")
            .center()
            .textBold()
            .bgColor("C0C0C0")
            .textFontSize(9)
            .create();
        RowRenderData data = Rows.of("1", "江西", "官方授权服务体验中心(武都新市街十字路口店)", "JX1111", "远帆", "OES", "T2", "2023年5月", "10,000",
            "4.000", "7,500", "21,500").center()
            .textFontSize(7)
            .rowAtleastHeight(1.8)
            .create();
        RowRenderData tail = Rows.of("合计(元)", "", "", "", "", "", "", "", "10,000", "4.000", "7,500", "21,500")
            .center()
            .textFontSize(7)
            .rowAtleastHeight(1.8)
            .create();
        MergeCellRule rule = MergeCellRule.builder().map(Grid.of(4, 0), Grid.of(4, 7)).build();
        map.put("table", Tables.of(header, data, data, data, tail).mergeRule(rule).create());
//        InputStream inputStream = new UrlResource(
//            "https://s3v2-qos.storage.wanyol.com/drp-commodity-dev/''/717e4c09-ada2-4d88-a7d7-132d9b77665a.docx").getInputStream();
        InputStream inputStream = Files.newInputStream(Paths.get(templateName));
        try (XWPFTemplate xwpfTemplate = XWPFTemplate.compile(inputStream)) {
            convert(xwpfTemplate.render(map).getXWPFDocument());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void convert(XWPFDocument doc) {
        String filename = "test.pdf";
        Path path = Paths.get(filename);
        try (OutputStream container = Files.newOutputStream(path, StandardOpenOption.WRITE,
            StandardOpenOption.CREATE); InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ,
            StandardOpenOption.DELETE_ON_CLOSE)) {
            IXWPFConverter<PdfOptions> converter = PdfConverter.getInstance();
            converter.convert(doc, container, PdfOptions.create().fontProvider(CNFont.INSTANCE));
            ItextPdfUtil.addWatermark(inputStream, Files.newOutputStream(Paths.get(target)), "一顿吃7碗");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void to(XWPFDocument doc) {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(target)));
        document.open();
        document.newPage();

        writer.setPageEmpty(true);
        writer.setPageEmpty(true);

        for (XWPFParagraph paragraphs : doc.getParagraphs()) {
            document.add(new Paragraph(paragraphs.getParagraphText()));
        }
        System.out.println("Document testing completed");
        doc.close();
        document.close();
//        writer.close();
    }
}
