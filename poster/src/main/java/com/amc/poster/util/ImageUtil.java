package com.amc.poster.util;

import com.amc.poster.model.ImageData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 图片工具类
 */
public class ImageUtil {

    /**
     * 将文件夹里面的所有图片转成数据
     */
    public static List<ImageData> getImagesData(String folderPath) {
        List<ImageData> result = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            try {
                for (File file : Objects.requireNonNull(folder.listFiles())) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".png")) {
                        fileName = fileName.substring(0, fileName.length() - 4);
                        BufferedImage img = ImageIO.read(file);
                        Rectangle rect = new Rectangle(img.getWidth(), img.getHeight());
                        int[][] rgbData = getRgbData(img, rect);
                        ArrayList<Rectangle> rects = PointUtil.getList(fileName);
                        result.add(new ImageData(fileName, rgbData, rects));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 大图找小图方法
     */
    public static Rectangle findRectangle(BufferedImage bigImage, List<ImageData> smallImagesData) {
        for (ImageData smallImageData : smallImagesData) {
            // 获取图片对比数据
            Rectangle imgRect = smallImageData.getImgRect();
            if (Objects.isNull(imgRect)) {
                continue;
            }
            int[][] bigRgbData = getRgbData(bigImage, imgRect);
            int[][] smallRgbData = smallImageData.getRgbData();
            // 初次匹配, 对比图片的四个顶点
            int xMax = imgRect.width - 1;
            int yMax = imgRect.height - 1;
            boolean check = equalsRGB(bigRgbData[0][0], smallRgbData[0][0]) &&
                            equalsRGB(bigRgbData[xMax][0], smallRgbData[xMax][0]) &&
                            equalsRGB(bigRgbData[xMax][yMax], smallRgbData[xMax][yMax]) &&
                            equalsRGB(bigRgbData[0][yMax], smallRgbData[0][yMax]);
            if (!check) {
                System.out.println("初次匹配, 对比图片的四个顶点，找不到跳过" + imgRect.width + "x" + imgRect.height);
                continue;
            }
            // 二次匹配, 对比图片的所有像素点
            System.out.println("二次匹配, 对比图片的所有像素点" + imgRect.width + "x" + imgRect.height);
            boolean find = true;
            a: for (int x = 0; x < xMax; x++) {
                for (int y = 0; y < yMax; y++) {
                    if (!equalsRGB(bigRgbData[x][y], smallRgbData[x][y])) {
                        find = false;
                        break a;
                    }
                }
            }
            // 确认找到图片
            if (find) {
                System.out.println("确认找到图片" + smallImageData.getPoint());
                return smallImageData.getPoint();
            }
        }
        System.out.println("未找到位置");
        System.out.println();
        return null;
    }

    /**
     * 图片转成数据
     */
    private static int[][] getRgbData(BufferedImage bufImage, Rectangle rect) {
        int width = rect.width;
        int height = rect.height;
        int[][] result = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = bufImage.getRGB(rect.x + x, rect.y + y);
            }
        }
        return result;
    }

    /**
     * 颜色匹配方法
     */
    private static boolean equalsRGB(int RGB1, int RGB2) {
        int sim = 5;
        int R1 = (RGB1 & 0xff0000) >> 16;
        int G1 = (RGB1 & 0xff00) >> 8;
        int B1 = (RGB1 & 0xff);
        int R2 = (RGB2 & 0xff0000) >> 16;
        int G2 = (RGB2 & 0xff00) >> 8;
        int B2 = (RGB2 & 0xff);

        return Math.abs(R1 - R2) < sim && Math.abs(G1 - G2) < sim && Math.abs(B1 - B2) < sim;
    }

}
