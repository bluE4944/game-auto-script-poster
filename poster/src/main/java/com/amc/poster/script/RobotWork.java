package com.amc.poster.script;

import com.amc.poster.constants.PosterConstant;
import com.amc.poster.model.ImageData;
import com.amc.poster.util.ImageUtil;
import com.amc.poster.util.MusicUtil;
import com.amc.poster.util.RobotUtil;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;

import javax.media.Player;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

/**
 * 简易脚本
 */
@RequiredArgsConstructor
public class RobotWork implements Runnable {

    private final String imagePath;
    private final int efficiency;
    private final Button node;

    public static Player player = null;

    @Override
    public void run() {
        // 获取图片数据
        List<ImageData> smallImagesData = ImageUtil.getImagesData(imagePath);
        if (smallImagesData.isEmpty()) {
            Platform.runLater(() -> node.setText("开刷"));
            return;
        }

        int clickSpaceMin = PosterConstant.posterInfo.getClickSpaceMin();
        int clickSpaceSize = PosterConstant.posterInfo.getClickSpaceMax() - clickSpaceMin;
        Point lastMousePoint = new Point(0, 0);
        int scanNum = 0;
        int maxScanNum = PosterConstant.posterInfo.getNoActionMaxTime() * 1000 / efficiency;

        // 开始刷本
        while (true) {
            try {
                scanNum++;
                Thread.sleep(efficiency);
            } catch (InterruptedException e) {
                break;
            }

            // 前台截图
            BufferedImage bigImage = RobotUtil.getDesktopImage();
            // 查找位置
            Rectangle rect = ImageUtil.findRectangle(bigImage, smallImagesData);

            if (Objects.isNull(rect)) {
                continue;
            }

            scanNum = 0;
            // 如果鼠标正在使用中, 则不需要移动
            Point mousePoint = RobotUtil.getMousePoint();
            if (mousePoint.x != lastMousePoint.x || mousePoint.y != lastMousePoint.y) {
                lastMousePoint = mousePoint;
                continue;
            }
            Platform.runLater(() -> node.setTextFill(Color.RED));
            // 计算鼠标位置
            int x = (int) (rect.x + Math.random() * rect.width);
            int y = (int) (rect.y + Math.random() * rect.height);
            // 记录鼠标位置
            lastMousePoint = new Point(x, y);
            // 计算鼠标单击效率
            int pressTime = (int) (clickSpaceMin + Math.random() * clickSpaceSize);
            RobotUtil.mouseClick(x, y, pressTime);
            Platform.runLater(() -> node.setTextFill(Color.BLACK));

//            if (maxScanNum > 0 && scanNum > maxScanNum) {
//                // 长时间未操作, 会播放音乐
//                player = MusicUtil.getPlayer(PosterConstant.getMusicPath());
//                MusicUtil.start(player);
//                break;
//            }
        }
    }

}
