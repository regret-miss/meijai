package com.mdd.admin.nail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 出图后处理：Unsharp Mask 轻量锐化。
 * 在生成结果存储前对图片做一次边缘锐化，不改变生成内容，仅增强边缘观感。
 * 任何异常都回退为原图，绝不阻塞生成流程。
 */
@Component
public class NailImagePostProcessor {
    private static final Logger log = LoggerFactory.getLogger(NailImagePostProcessor.class);

    public byte[] sharpen(byte[] bytes, String mimeType, double amount) {
        if (bytes == null || bytes.length == 0 || amount <= 0) return bytes;
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) return bytes;
            BufferedImage result = unsharpMask(source, (float) amount);
            String format = "image/jpeg".equals(mimeType) ? "jpg" : "png";
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            if (!ImageIO.write(result, format, buffer)) return bytes;
            return buffer.toByteArray();
        } catch (Exception error) {
            log.warn("美甲图片锐化失败，使用原图：{}", error.getMessage());
            return bytes;
        }
    }

    private BufferedImage unsharpMask(BufferedImage source, float amount) {
        int width = source.getWidth();
        int height = source.getHeight();
        // 5x5 归一化高斯核（sigma≈1.4）
        float[] kernel = {
                1f, 4f, 6f, 4f, 1f,
                4f, 16f, 24f, 16f, 4f,
                6f, 24f, 36f, 24f, 6f,
                4f, 16f, 24f, 16f, 4f,
                1f, 4f, 6f, 4f, 1f
        };
        float sum = 0f;
        for (float value : kernel) sum += value;
        for (int i = 0; i < kernel.length; i++) kernel[i] /= sum;

        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D copy = rgb.createGraphics();
        copy.drawImage(source, 0, 0, null);
        copy.dispose();

        BufferedImage blurred = new ConvolveOp(new Kernel(5, 5, kernel), ConvolveOp.EDGE_NO_OP, null).filter(rgb, null);

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgbPixel = rgb.getRGB(x, y);
                int blurPixel = blurred.getRGB(x, y);
                int r = clamp(((rgbPixel >> 16) & 0xFF) + (int) (amount * (((rgbPixel >> 16) & 0xFF) - ((blurPixel >> 16) & 0xFF))));
                int g = clamp(((rgbPixel >> 8) & 0xFF) + (int) (amount * (((rgbPixel >> 8) & 0xFF) - ((blurPixel >> 8) & 0xFF))));
                int b = clamp((rgbPixel & 0xFF) + (int) (amount * ((rgbPixel & 0xFF) - (blurPixel & 0xFF))));
                out.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
