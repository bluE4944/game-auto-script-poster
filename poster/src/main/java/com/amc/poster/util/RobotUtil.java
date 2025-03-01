package com.amc.poster.util;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

/**
 * Robot工具类
 */
public class RobotUtil {

    private static final Robot robot = getRobot();

    /**
     * 获取桌面截图
     */
    public static BufferedImage getDesktopImage() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        return robot.createScreenCapture(screenRectangle);
    }

    /**
     * 获取当前鼠标位置
     */
    public static Point getMousePoint() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    /**
     * 获取指定位置的颜色
     */
    public static Color getPointColor(Point point) {
        return robot.getPixelColor(point.x, point.y);
    }

    /**
     * 单击桌面的指定位置
     */
    public static void mouseClick(int x, int y, long pressTime) {
        try {
            // 需要调用多次才能移动到正确位置
            for (int i = 0; i < 6; i++) {
                robot.mouseMove(x, y);
            }
            robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(pressTime);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Robot getRobot() {
        try {
            return new Robot();
        } catch (Exception e) {
            throw new RuntimeException("Robot创建失败");
        }
    }

}
