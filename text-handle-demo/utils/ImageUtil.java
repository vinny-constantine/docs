package com.dover.pdf.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author dover
 * @since 2022/8/4
 */
public class ImageUtil {

    public static final String[] imageMimeTypes = new String[]{"image/bmp", "image/gif", "image/jpeg", "image/jpg", "image/png", "image/webp"};
    public static final String[] imageSuffix = new String[]{"bmp", "gif", "jpeg", "jpg", "png", "webp"};

    /**
     * 是否为图片
     * true：是图片，false：不是图片
     */
    public static boolean isImage(InputStream inputStream) {
        try {
            if (inputStream == null) return false;
            return Arrays.binarySearch(imageMimeTypes, new Tika().detect(inputStream)) >= 0;
        } catch (IOException e) {
            throw new RuntimeException("UPLOADED_FILE_WAS_BROKEN", e);
        }
    }

    /**
     * 是否非图片
     * true：不是图片，false：是图片
     */
    public static boolean isNotImage(InputStream inputStream) {
        return !isImage(inputStream);
    }

    /**
     * 是否为图片
     * true：是图片，false：不是图片
     */
    public static boolean isImage(String contentType) {
        if (contentType == null) return false;
        return Arrays.binarySearch(imageMimeTypes, contentType) >= 0;
    }

    /**
     * 是否非图片
     * true：不是图片，false：是图片
     */
    public static boolean isNotImage(String contentType) {
        return !isImage(contentType);
    }

    /**
     * 是否为图片
     * true：是图片，false：不是图片
     */
    public static boolean isImageBySuffix(String suffix) {
        if (suffix == null) return false;
        return Arrays.binarySearch(imageSuffix, suffix) >= 0;
    }

    /**
     * 是否非图片
     * true：不是图片，false：是图片
     */
    public static boolean isNotImageBySuffix(String suffix) {
        return !isImageBySuffix(suffix);
    }

}
