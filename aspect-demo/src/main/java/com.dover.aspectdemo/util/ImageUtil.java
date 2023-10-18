package com.dover.aspectdemo.util;

import lombok.SneakyThrows;
import org.apache.tika.Tika;

import java.io.InputStream;
import java.util.Arrays;

/**
 * @author dover
 * @since 2022/8/4
 */
public class ImageUtil {

    public static final String[] imageMimeTypes = new String[]{"image/bmp", "image/gif", "image/jpeg", "image/jpg", "image/png", "image/webp"};

    @SneakyThrows
    public static boolean isImage(InputStream inputStream) {
        return Arrays.binarySearch(imageMimeTypes, new Tika().detect(inputStream)) >= 0;
    }

}
