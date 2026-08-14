package com.mdd.admin.nail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;

/**
 * 启发式美学打分（零依赖）：
 * 综合 清晰度（Laplacian 方差）、色彩丰富度（Hasler-Süsstrunk）、曝光适中度 三项指标，
 * 输出 1-10 分。用于同一任务多张结果间的相对排序（把最锐利、最饱满的放前面）。
 * 任何异常返回 0，不影响生成流程。
 */
@Component
public class NailHeuristicScorer {
    private static final Logger log = LoggerFactory.getLogger(NailHeuristicScorer.class);
    private static final int TARGET_EDGE = 256;

    public double score(byte[] bytes) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) return 0;
            BufferedImage small = scale(source, TARGET_EDGE);
            double sharpness = clamp01(laplacianVariance(small) / 60.0);
            double colorfulness = clamp01(colorfulness(small) / 70.0);
            double brightness = brightness(small);
            double exposure = 1.0 - clamp01(Math.abs(brightness - 0.5) * 2.0);
            double composite = 0.5 * sharpness + 0.3 * colorfulness + 0.2 * exposure;
            return Math.round((1.0 + composite * 9.0) * 10.0) / 10.0;
        } catch (Exception error) {
            log.warn("美甲图片启发式打分失败：{}", error.getMessage());
            return 0;
        }
    }

    private BufferedImage scale(BufferedImage source, int maxEdge) {
        double ratio = Math.min(1d, (double) maxEdge / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private double laplacianVariance(BufferedImage image) {
        float[] kernel = {0f, 1f, 0f, 1f, -4f, 1f, 0f, 1f, 0f};
        BufferedImage filtered = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null).filter(image, null);
        int width = filtered.getWidth();
        int height = filtered.getHeight();
        long sum = 0;
        long sumSq = 0;
        int count = width * height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = filtered.getRGB(x, y);
                int gray = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                sum += gray;
                sumSq += (long) gray * gray;
            }
        }
        double mean = (double) sum / count;
        return (double) sumSq / count - mean * mean;
    }

    private double colorfulness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        long sumRg = 0, sumYb = 0, sumRgSq = 0, sumYbSq = 0;
        int count = width * height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                double rg = r - g;
                double yb = 0.5 * (r + g) - b;
                sumRg += (long) rg;
                sumYb += (long) yb;
                sumRgSq += (long) (rg * rg);
                sumYbSq += (long) (yb * yb);
            }
        }
        double meanRg = (double) sumRg / count;
        double meanYb = (double) sumYb / count;
        double stdRg = Math.sqrt((double) sumRgSq / count - meanRg * meanRg);
        double stdYb = Math.sqrt((double) sumYbSq / count - meanYb * meanYb);
        return Math.sqrt(stdRg * stdRg + stdYb * stdYb) + 0.3 * Math.sqrt(meanRg * meanRg + meanYb * meanYb);
    }

    private double brightness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        long sum = 0;
        int count = width * height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                sum += ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
            }
        }
        return (double) sum / (count * 3 * 255);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
