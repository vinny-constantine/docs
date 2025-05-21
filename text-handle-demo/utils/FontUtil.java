package com.dover.util;

import com.google.typography.font.sfntly.Font;
import com.google.typography.font.sfntly.Font.PlatformId;
import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.Tag;
import com.google.typography.font.sfntly.table.core.CMap;
import com.google.typography.font.sfntly.table.core.CMapTable;
import com.google.typography.font.tools.subsetter.RenumberingSubsetter;
import com.google.typography.font.tools.subsetter.Subsetter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.types.resources.URLResource;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 裁剪字体包工具类
 *
 * @author dover
 * @since 2021/9/8
 */
public final class FontUtil {


    public static class FontResourcesHolder {

        public static final String DEFAULT_FONT_URL = "https://localhost:8080/16c0738c-25e3-4188-a060-bfb38316e973.ttf";
        public static final String FONT_URL_NACOS = "font-url";
        public static final String FONT_NAME = "zh_CN.ttf";
        private static final CMap C_MAP;
        private static final Font FONT;
        private static final FontFactory FONT_FACTORY;

        static {
            try {
                FONT_FACTORY = FontFactory.getInstance();
                URLResource resources = new URLResource(DrpProperty.get(FONT_URL_NACOS, DEFAULT_FONT_URL));
                FONT = FONT_FACTORY.loadFonts(resources.getInputStream())[0];
                CMapTable cMapTable = FONT.getTable(Tag.cmap);
                C_MAP = cMapTable.cmap(PlatformId.Windows.value(), Font.WindowsEncodingId.UnicodeUCS2.value());
            } catch (Exception e) {
                throw new RuntimeException("加载字体包失败", e);
            }
        }

        private FontResourcesHolder() {
        }


    }


    @SneakyThrows
    public static void main(String[] args) {
        extractSubFont(stringToCodePoints("马一一二二"),
            Files.newOutputStream(Paths.get("D:\\Users\\80321000\\Desktop\\A.ttf")));
    }

    /**
     * 抽取出字体子集包
     *
     * @param str          字符串
     * @param outputStream 输出流
     */
    public static void extract(String str, OutputStream outputStream) {
        extractSubFont(stringToCodePoints(str), outputStream);
    }

    /**
     * 字符串转16进制内码
     *
     * @param str ab一2?仯3?4
     * @return \\u61\\u62\\u4e00\\u32\\u2b802\\u4eef\\u33\\u2b82f\\u34\\u34
     */
    public static String stringToCodePoints(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        str.codePoints().forEach(cp -> stringBuilder.append("\\u").append(Integer.toHexString(cp)));
        return stringBuilder.toString();
    }

    /**
     * 裁剪出字体子集
     *
     * @param unicodeStr 必须为内码，如：\\u4e00\\u2596B
     */
    public static void extractSubFont(String unicodeStr, OutputStream outputStream) {
        Set<Integer> glyphs = new LinkedHashSet<>();
        try {
            for (String code : unicodeStr.split("\\\\u")) {
                if (StringUtils.isEmpty(code)) continue;
                int glyphId = FontResourcesHolder.C_MAP.glyphId(Integer.parseInt(code, 16));
                if (glyphId != 0) {
                    glyphs.add(glyphId);
                }
            }
            Subsetter subsetter = new RenumberingSubsetter(FontResourcesHolder.FONT, FontResourcesHolder.FONT_FACTORY);
            subsetter.setGlyphs(new ArrayList<>(glyphs));
            Font newFont = subsetter.subset().build();
            FontResourcesHolder.FONT_FACTORY.serializeFont(newFont, outputStream);
        } catch (Exception e) {
            DrpLog.info("抽取字体出错", e);
        }
    }
}
