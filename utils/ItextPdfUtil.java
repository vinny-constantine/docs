package com.dover.pdf.util;

import com.lowagie.text.DocumentException;
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author dover
 * @since 2021/8/27
 */
public class ItextPdfUtil {


    public static Font FONT_CN_16;
    public static Font FONT_CN_20;
    public static Font FONT_CN_24;
    public static Font FONT_CN_32;

    static {
        try {
            byte[] fontBytes = IOUtils.toByteArray(
                ItextPdfUtil.class.getClassLoader().getResourceAsStream("zh_CN.ttf"));
            FONT_CN_16 = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fontBytes))
                .deriveFont(Font.PLAIN, 16F);
            FONT_CN_20 = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fontBytes))
                .deriveFont(Font.PLAIN, 20F);
            FONT_CN_24 = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fontBytes))
                .deriveFont(Font.PLAIN, 24F);
            FONT_CN_32 = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(fontBytes))
                .deriveFont(Font.PLAIN, 32F);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SneakyThrows
    public static void main(String[] args) {
        // 要输出的pdf文件
        final String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
        // 将pdf文件先加水印然后输出
        addWatermark(new FileInputStream("D:\\Users\\80321000\\Desktop\\A.pdf"),
            new FileOutputStream("D:\\Users\\80321000\\Desktop\\B.pdf"), "  下载使用人：测试user");
    }


    public static void addWatermark(InputStream input, OutputStream output,
        String waterMark) throws DocumentException, IOException {
        BaseFont font = BaseFont.createFont("zh_CN.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
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
            content.setFontAndSize(font, 48);
            // 获取每一页的高度、宽度
            pageSizeWithRotation = reader.getPageSizeWithRotation(i);
            float pageH = pageSizeWithRotation.getHeight();
            float pageW = pageSizeWithRotation.getWidth();
            for (int height = textH - 5; height < pageH; height += textH * 20) {
                for (int width = textW; width < pageW + textW; width += textW * 4) {
                    content.showTextAligned(Element.ALIGN_CENTER, waterMark, width - textW, height - textH, 30);
                }
            }
            content.endText();

        }
        stamper.close();
    }


}
