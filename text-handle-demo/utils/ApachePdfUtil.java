package com.dover.pdf.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDPageAdditionalActions;
import org.apache.pdfbox.util.Matrix;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author dover
 * @since 2023/6/30
 */
public class ApachePdfUtil {
    public static Font FONT_CN;
    public static InputStream FONT_INPUT_STREAM;

    static {
        try {
            FONT_CN = Font.createFont(Font.TRUETYPE_FONT,
                ItextPdfUtil.class.getClassLoader().getResourceAsStream("zh_CN.ttf")).deriveFont(Font.PLAIN, 24F);
            FONT_INPUT_STREAM = ItextPdfUtil.class.getClassLoader().getResourceAsStream("zh_CN.ttf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String filename = "D:\\Users\\80321000\\Desktop\\a.pdf";
    public static final String templateName = "D:\\Users\\80321000\\Desktop\\template.pdf";


    public static void main(String[] args) throws IOException {
        try (PDDocument doc = Loader.loadPDF(Files.newInputStream(Paths.get(templateName)))) {
            PDPage page = doc.getPage(0);
            PDFont font = PDType0Font.load(doc, FONT_INPUT_STREAM);
            PDRectangle artBox = page.getArtBox();
            PDRectangle bBox = page.getBBox();
            PDPageAdditionalActions actions = page.getActions();
            PDRectangle bleedBox = page.getBleedBox();
            PDRectangle mediaBox = page.getMediaBox();
            COSDictionary cosObject = page.getCOSObject();
            Matrix matrix = page.getMatrix();
            PDMetadata metadata = page.getMetadata();
            PDRectangle trimBox = page.getTrimBox();
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.moveTo(1, 0);
                contents.beginText();
                contents.setFont(font, 12);
                contents.showText("我爱吃饭我爱吃饭我爱吃饭我爱吃饭我爱吃饭我爱吃饭");
                contents.endText();
            }
            doc.save(filename);
        }
    }
}
