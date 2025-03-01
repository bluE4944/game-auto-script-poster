package com.amc.poster.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * open cv 图片匹配器
 * @author Li
 * @since 2025年3月1日 11点21分
 * @version 1.0
 */
public class OpenCVImageMatcher {
    static {
        // 设置 OpenCV 本地库路径
//        System.setProperty("java.library.path", "lib/opencv/3416");
        // 加载 OpenCV 库
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // 加载动态库
        URL url = ClassLoader.getSystemResource("lib/opencv/3416/opencv_java3416.dll");
        System.load(url.getPath());
    }

    public static List<Point> findImage(Mat screenshot, Mat template) {
        // 检查图像和模板的尺寸
        if (ObjectUtil.hasEmpty(screenshot, template) || screenshot.empty() || template.empty()) {
            System.out.println("图像或模板读取失败，请检查文件路径。");
            return null;
        }

        if (template.height() > screenshot.height() || template.width() > screenshot.width()) {
            System.out.println("模板的尺寸不能大于图像的尺寸，请更换模板或图像。");
            return null;
        }

        System.out.println("图像尺寸: " + screenshot.width() + "x" + screenshot.height());
        System.out.println("模板尺寸: " + template.width() + "x" + template.height());

        List<Point> points = new ArrayList<>();
        Mat result = new Mat();
        int resultCols = screenshot.cols() - template.cols() + 1;
        int resultRows = screenshot.rows() - template.rows() + 1;
        result.create(resultRows, resultCols, CvType.CV_32FC1);

        // 使用模板匹配方法
        Imgproc.matchTemplate(screenshot, template, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // 匹配阈值
        double threshold = 0.8;
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc;
        while (mmr.maxVal >= threshold) {
            matchLoc = mmr.maxLoc;
            points.add(matchLoc);
            // 将已匹配区域置为 0，避免重复匹配
            Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + template.cols(), matchLoc.y + template.rows()), new Scalar(0), -1);
            mmr = Core.minMaxLoc(result);
        }
        return points;
    }

    /**
     * img path to mat
     * @param path img path
     * @return mat
     */
    public static Mat imgPathToMat(String path) {
        // 1.将文件转为bytes
        byte[] byteArray = IoUtil.readBytes(FileUtil.getInputStream(path));
        // 2.将bytes转为MatOfByte
        MatOfByte matOfByte = new MatOfByte(byteArray);
        // 3.将MatOfByte解码为Mat
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
    }

    public static void main(String[] args) throws IOException {
//        Mat template = Imgcodecs.imread("D:\\workspace\\ui-project\\poster\\src\\main\\resources\\img\\LegendsImg\\$25887008.9896119$26542413.2424881.png");
//        Mat screenshot = Imgcodecs.imread("D:\\workspace\\ui-project\\poster\\src\\main\\resources\\img\\other\\allSizeL.png");
        Mat template = imgPathToMat("D:\\workspace\\ui-project\\poster\\src\\main\\resources\\img\\LegendsImg\\$25887008.9896119$26542413.2424881.png");
        Mat screenshot = imgPathToMat("D:\\workspace\\ui-project\\poster\\src\\main\\resources\\img\\other\\allSizeL.png");
        List<Point> points = findImage(screenshot, template);
        if (Objects.isNull(points)) {
            return;
        }
        for (Point point : points) {
            System.out.println("Found at: " + point.x + ", " + point.y);
        }
    }
}