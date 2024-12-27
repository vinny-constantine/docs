package com.dover.pdf.util;

import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import javax.swing.JLabel;
import java.awt.FontMetrics;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 水印工具类
 *
 * @author dover
 * @since 2021/8/31
 */
public class WaterMarkUtil {

    public static final String FONT_NAME = "zh_CN.ttf";
    private static BaseFont BASE_FONT;

    static {
        try {
            URLResource resources = new URLResource(
                Propertys.get(FontResourcesHolder.FONT_URL_NACOS, FontResourcesHolder.DEFAULT_FONT_URL));
            BASE_FONT = BaseFont.createFont(FONT_NAME, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, true,
                IOUtils.toByteArray(resources.getInputStream()), null, false);
        } catch (Exception e) {
            logger.info("加载字体包失败", e);
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        addForPdf(new FileInputStream("D:\\Users\\80321000\\Desktop\\A.pdf"),
            new FileOutputStream("D:\\Users\\80321000\\Desktop\\B.pdf"), "示例1234");
    }

    /**
     * 给 pdf 添加水印
     *
     * @param input     输入流
     * @param output    输出流
     * @param waterMark 水印文本
     */
    @SneakyThrows
    public static void addForPdf(InputStream input, OutputStream output, String waterMark) {
        PdfReader reader = new PdfReader(input);
        PdfStamper stamper = new PdfStamper(reader, new BufferedOutputStream(output));
        // 获取水印文本的高度和宽度
        JLabel label = new JLabel();
        label.setText(waterMark);
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        int textH = metrics.getHeight();
        int textW = metrics.stringWidth(label.getText());
        // 设置水印透明度
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.1f);
        gs.setStrokeOpacity(0.1f);
        PdfContentByte content;
        Rectangle pageSizeWithRotation;
        for (int i = 1; i < reader.getNumberOfPages() + 1; i++) {
            content = stamper.getOverContent(i); // 在内容上方加水印
            // content = stamper.getUnderContent(i); // 在内容下方加水印
            content.saveState();
            content.setGState(gs);
            content.beginText();
            content.setFontAndSize(BASE_FONT, 30);
            // 获取每一页的高度、宽度
            pageSizeWithRotation = reader.getPageSizeWithRotation(i);
            float pageH = pageSizeWithRotation.getHeight();
            float pageW = pageSizeWithRotation.getWidth();
            for (int height = textH - 5; height < pageH; height += textH * 14) {
                for (int width = textW; width < pageW + textW; width += textW * 3) {
                    content.showTextAligned(Element.ALIGN_CENTER, waterMark, width - textW, height - textH, 30);
                }
            }
            content.endText();

        }
        stamper.close();
    }
}
