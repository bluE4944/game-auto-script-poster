package com.amc.poster.script;

import com.amc.poster.constants.PosterConstant;
import com.amc.poster.model.ImageData;
import com.amc.poster.util.ImageUtil;
import com.amc.poster.util.MusicUtil;
import com.amc.poster.util.WindowUtil;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;

import javax.media.Player;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 简易脚本
 */
@RequiredArgsConstructor
public class SimpleWork implements Runnable {

	private final HWND hwnd;
	private final String imgPath;
	private final double scaling;
    private final int efficiency;
	private final Button node;

	public static volatile boolean isRun = true;
    public static List<Player> players = new ArrayList<>();

	@Override
	public void run() {
		// 获取图片数据
		List<ImageData> smallImagesData = ImageUtil.getImagesData(imgPath);
		if (smallImagesData.isEmpty()) {
			return;
		}

		int clickSpaceMin = PosterConstant.posterInfo.getClickSpaceMin();
		int clickSpaceSize = PosterConstant.posterInfo.getClickSpaceMax() - clickSpaceMin;
		int scanNum = 0;
		int maxScanNum = PosterConstant.posterInfo.getNoActionMaxTime() * 1000 / efficiency;

		// 开始刷本
		while (true) {
			try {
				// 检查关闭
				if (!isRun)  {
					break;
				}
				// 控制效率
				scanNum++;
				Thread.sleep(efficiency);
				// 后台截图
				BufferedImage bigImage = WindowUtil.getWinImage(hwnd);
				// 查找位置
				Rectangle rect = ImageUtil.findRectangle(bigImage, smallImagesData);

				if (rect != null) {
					Platform.runLater(() -> node.setTextFill(Color.RED));
					// 计算鼠标位置
					int x = (int) ((int) (rect.x + Math.random() * rect.width) * scaling);
					int y = (int) ((int) (rect.y + Math.random() * rect.height) * scaling);
					// 计算鼠标单击效率
					int pressTime = (int) (clickSpaceMin + Math.random() * clickSpaceSize);
					WindowUtil.mouseClick(hwnd, x, y, pressTime);
					Platform.runLater(() -> node.setTextFill(Color.BLACK));
					scanNum = 0;
				}

				if (maxScanNum > 0 && scanNum > maxScanNum) {
					// 长时间未操作, 会播放音乐
					Player player = MusicUtil.getPlayer(PosterConstant.getMusicPath());
					MusicUtil.start(player);
					players.add(player);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
