package com.dover.pdf.util;

import com.dover.pdf.util.ItextPdfUtil;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author dover
 * @since 2021/9/1
 */
@Component
public class ImageMergeUtil {

    public static final String imagePath = "D:\\Users\\80321000\\Desktop\\img.png";
    public static final String top = "D:\\Users\\80321000\\Pictures\\top.png";
    public static final String bg = "D:\\Users\\80321000\\Pictures\\bg.png";
    public static final String person = "D:\\Users\\80321000\\Pictures\\person.png";
    public static final String qr = "D:\\Users\\80321000\\Pictures\\qr.png";


    public static void main(String[] args) throws Exception {
        BufferedImage bufferedImage = run();
        ImageIO.write(bufferedImage, "png", new FileOutputStream(imagePath));
    }

    public static BufferedImage run() throws Exception {
        BufferedImage topBgImg = ImageIO.read(new FileInputStream(top));
        BufferedImage bottomBgImg = ImageIO.read(new FileInputStream(bg));
        BufferedImage personImg = ImageIO.read(new FileInputStream(person));
        BufferedImage qrImg = ImageIO.read(new FileInputStream(qr));
        int totalWidth = topBgImg.getWidth();
        int totalHeight = topBgImg.getHeight() * 180 / 100;
        BufferedImage image = topBgImg.createGraphics()
            .getDeviceConfiguration()
            .createCompatibleImage(topBgImg.getWidth(), totalHeight, Transparency.TRANSLUCENT);
//        BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        // 二维码宽高
        int qrWidth = totalWidth * 14 / 100;
        // 二维码，将二维码裁剪后画入固定大小的画布上
        BufferedImage qrLayer = new BufferedImage(qrWidth, qrWidth, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pen = qrLayer.createGraphics();
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.setClip(new Ellipse2D.Double(0, 0, qrWidth, qrWidth));
        pen.drawImage(qrImg, 0, 0, qrWidth, qrWidth, null);
        pen.dispose();
        // 主图
        pen = image.createGraphics();
        pen.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 背景图上半部分
        pen.drawImage(topBgImg, 0, 0, totalWidth, topBgImg.getHeight(), null);
        // 背景图下半部分
        pen.drawImage(bottomBgImg, 0, topBgImg.getHeight(), totalWidth, totalHeight, 10, bottomBgImg.getHeight() / 10,
            bottomBgImg.getWidth(), bottomBgImg.getHeight(), null);
        pen.dispose();
        // 二维码背景
        int qrCodeX = totalWidth / 10;
        int qrCodeY = totalHeight * 54 / 100;
        int qrCodeOffset = 2;
        int qrCodeBgWidth = qrWidth + qrCodeOffset * 2;
        pen = image.createGraphics();
        pen.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
        pen.setColor(Color.WHITE);
        pen.fill(new Ellipse2D.Double(qrCodeX, qrCodeY, qrCodeBgWidth, qrCodeBgWidth));
        pen.dispose();
        // 二维码
        pen = image.createGraphics();
        pen.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.setClip(new Ellipse2D.Double(qrCodeX + qrCodeOffset, qrCodeY + qrCodeOffset, qrWidth, qrWidth));
        pen.drawImage(qrLayer, qrCodeX + qrCodeOffset, qrCodeY + qrCodeOffset, null);
        pen.dispose();
        // 二维码右侧文字
        pen = image.createGraphics();
        pen.setFont(ItextPdfUtil.FONT_CN_16);
        pen.setStroke(new BasicStroke(1));
        pen.setColor(Color.BLACK);
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.drawString("长按识别小程序码", qrCodeX + qrWidth, qrCodeY + qrWidth * 60 / 100);
        pen.dispose();
        // 二维码下侧文字
        pen = image.createGraphics();
        pen.setFont(ItextPdfUtil.FONT_CN_32);
        pen.setStroke(new BasicStroke(1.5F));
        pen.setColor(Color.BLACK);
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.drawString("Find N3 | 轻巧好用 出片专用", qrCodeX, qrCodeY + qrWidth * 2);
        pen.dispose();
        // 导购头像
        int avatarX = qrCodeX;
        int avatarY = totalHeight * 85 / 100;
        int avatarWidth = qrWidth * 80 / 100;
        pen = image.createGraphics();
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.setClip(new Ellipse2D.Double(avatarX, avatarY, avatarWidth, avatarWidth));
        pen.drawImage(personImg, avatarX, avatarY, avatarWidth, avatarWidth, null);
        pen.dispose();
        // 导购右侧文字
        pen = image.createGraphics();
        pen.setFont(ItextPdfUtil.FONT_CN_16);
        pen.setStroke(new BasicStroke(1));
        pen.setColor(Color.BLACK);
        pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pen.drawString("张婷婷 ｜ 官方授权体验店张婷婷 ｜ 官方授权体验店张婷婷 ｜ 官方授权体验店张婷婷 ｜ 官方授权体验店张婷婷 ｜ 官方授权体验店", avatarX + avatarWidth, avatarY + avatarWidth * 60 / 100);
        pen.dispose();
        return image;
    }
}
