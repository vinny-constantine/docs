package com.dover.pdf.font;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.UrlResource;

import java.awt.Color;

/**
 * @author dover
 * @since 2023/7/3
 */
public class CNFont implements IFontProvider {

    public static final String DEFAULT_FONT_URL = "https://localhost:8080/file/830e1736-a96e-4437-be9c-6545ef411c88.ttf";
    public static final String FONT_NAME = "zh_CN.ttf";
    public static final BaseFont BASE_FONT;

    static {
        try {
//            BASE_FONT = BaseFont.createFont(FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
            BASE_FONT = BaseFont.createFont(FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true,
                IOUtils.toByteArray(new UrlResource(DEFAULT_FONT_URL).getInputStream()), null, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CNFont() {

    }

    public static final CNFont INSTANCE = new CNFont();


    @Override
    public Font getFont(String familyName, String encoding, float size, int style, Color color) {
        try {
            return new Font(BASE_FONT, size, style, color);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
