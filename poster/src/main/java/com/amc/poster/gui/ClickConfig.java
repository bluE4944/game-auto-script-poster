package com.amc.poster.gui;

import cn.hutool.core.util.StrUtil;
import com.amc.javafx.annotations.BindView;
import com.amc.javafx.annotations.OnClick;
import com.amc.javafx.annotations.Stop;
import com.amc.javafx.util.ViewUtil;
import com.amc.poster.constants.PosterConstant;
import com.amc.poster.core.properties.PosterProperties;
import com.amc.poster.model.PosterInfo;
import com.amc.poster.model.ViewInfo;
import com.amc.poster.script.RobotWork;
import com.amc.poster.script.SimpleWork;
import com.amc.poster.util.ElementApi;
import com.amc.poster.util.RobotUtil;
import com.amc.poster.util.WindowUtil;
import com.amc.util.file.PropertiesUtil;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.WinDef;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import javax.media.Player;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author AMC
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ClickConfig {

    private static final String PATH_PREFIX = System.getProperty("user.dir");
    private final PosterProperties posterProperties;

    @BindView
    private static ViewInfo viewInfo;
    private Thread robotWorkThread;

    @OnClick
    public void selectTitleButton() {
        String titleText = viewInfo.getTitleTextField();
        List<String> titles = new ArrayList<>();
        WindowUtil.getAllWin().stream().map(DesktopWindow::getTitle).collect(Collectors.toList())
                .forEach(text -> {
                    if (text.contains(titleText)) {
                        titles.add(0, text);
                    } else {
                        titles.add(text);
                    }
                });
        String selectTitle = ElementApi.showChoiceDialog(titles);
        if(selectTitle != null) {
            viewInfo.setTitleTextField(selectTitle);
        }
    }

    @Async
    @OnClick
    public void bindTitleButton() throws Exception {
        for (int i = 2; i > 0; i--) {
            String title = "将在" + i + "秒后获取当前窗口标题";
            viewInfo.setTitleTextField(title);
            Thread.sleep(1000);
        }
        WinDef.HWND hwnd = WindowUtil.getActiveHwnd();
        String selectTitle = WindowUtil.getWinTitle(hwnd);
        viewInfo.setTitleTextField(selectTitle);
    }

    @OnClick
    public void selectImgPathButton() {
        File selectedFile = ElementApi.showDirectoryChooser(PATH_PREFIX);
        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            if (path.startsWith(PATH_PREFIX)) {
                path = path.substring(PATH_PREFIX.length());
                viewInfo.setImgPathTextField(path);
            }
        }
    }

    @OnClick
    public void takeImgButton() {
        String imgPath = viewInfo.getImgPathTextField();
        if (isIllegalPath(imgPath)) {
            return;
        }
        WinDef.HWND hwnd = getHwnd();
        if (hwnd == null) {
            return;
        }

        BufferedImage image = WindowUtil.getWinImage(hwnd);
        ElementApi.showImageView(image, PATH_PREFIX + imgPath);
    }

    @OnClick
    public void startButton(Button startButton) {
        String imgPath = viewInfo.getImgPathTextField();
        if (isIllegalPath(imgPath)) {
            return;
        }
        WinDef.HWND hwnd = getHwnd();
        if (hwnd == null) {
            return;
        }

        SimpleWork.isRun = true;
        SimpleWork work = new SimpleWork(hwnd, PATH_PREFIX + imgPath, viewInfo.getScalingChoiceBox(), viewInfo.getEfficiencyTextField(), startButton);
        new Thread(work).start();

        ViewUtil.showMessageDialog("已开启一个任务");
    }

    @OnClick
    public void stopButton() {
        SimpleWork.isRun = false;
        SimpleWork.players.forEach(Player::close);
        SimpleWork.players.clear();
        ViewUtil.showMessageDialog("已停止全部任务");
    }

    @OnClick
    public void selectImagePathButton() {
        File selectedFile = ElementApi.showDirectoryChooser(PATH_PREFIX);
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            if (imagePath.startsWith(PATH_PREFIX)) {
                imagePath = imagePath.substring(PATH_PREFIX.length());
                viewInfo.setImagePathTextField(imagePath);
            }
        }
    }

    @OnClick
    public void takeImageButton(Stage window) throws Exception {
        String imagePath = viewInfo.getImagePathTextField();
        if (isIllegalPath(imagePath)) {
            return;
        }

        window.setIconified(true);
        Thread.sleep(300);
        BufferedImage desktopImage = RobotUtil.getDesktopImage();
        ElementApi.showImageView(desktopImage, PATH_PREFIX + imagePath);
    }

    @OnClick
    public void startOrStopButton(Button startOrStopButton) {
        String text = startOrStopButton.getText();

        if (Objects.equals(text, "开刷")) {
            String imagePath = viewInfo.getImagePathTextField();
            if (isIllegalPath(imagePath)) {
                return;
            }

            startOrStopButton.setText("停刷");
            Integer efficiency = viewInfo.getEfficiencyTextField();
            robotWorkThread = new Thread(new RobotWork(PATH_PREFIX + imagePath, efficiency, startOrStopButton));
            robotWorkThread.start();
        }
        else {
            startOrStopButton.setText("开刷");
            robotWorkThread.interrupt();
            if (RobotWork.player != null) {
                RobotWork.player.close();
                RobotWork.player = null;
            }
        }
    }

//    @Stop
//    public void stop(Stage window) {
//        PosterInfo posterInfo = PosterConstant.posterInfo;
//
//        posterInfo.setX((int) window.getX());
//        posterInfo.setY((int) window.getY());
//        posterInfo.setTitle(viewInfo.getTitleTextField());
//        posterInfo.setImgPath(viewInfo.getImgPathTextField());
//        posterInfo.setScaling(viewInfo.getScalingChoiceBox());
//        posterInfo.setEfficiency(viewInfo.getEfficiencyTextField());
//        posterInfo.setImagePath(viewInfo.getImagePathTextField());
//
//        PropertiesUtil.saveModel(PosterConstant.propertyPath, posterInfo);
//        System.exit(0);
//    }

    @Stop
    public void stop(Stage window) {

        posterProperties.setLocationX((int) window.getX());
        posterProperties.setLocationY((int) window.getY());
        posterProperties.setTitle(viewInfo.getTitleTextField());
        posterProperties.setBackendImgPath(viewInfo.getImgPathTextField());
        posterProperties.setScaling(viewInfo.getScalingChoiceBox());
        posterProperties.setEfficiency(viewInfo.getEfficiencyTextField());
        posterProperties.setFontImgPath(viewInfo.getImagePathTextField());

        PropertiesUtil.saveModel(PosterConstant.propertyPath, posterProperties);
        System.exit(0);
    }

    private WinDef.HWND getHwnd() {
        String title = viewInfo.getTitleTextField();
        if (StringUtils.hasText(title)) {
            WinDef.HWND hwnd = WindowUtil.getHwnd(title);
            if (hwnd != null) {
                return hwnd;
            }
        }
        ViewUtil.showMessageDialog("请先选择/绑定窗口标题");
        return null;
    }

    private boolean isIllegalPath(String path) {
        if (StrUtil.isEmpty(path)) {
            ViewUtil.showMessageDialog("请先选择图片文件夹路径");
            return true;
        }
        if (!new File(PATH_PREFIX + path).exists()) {
            ViewUtil.showMessageDialog("请选择正确的文件夹路径");
            return true;
        }
        return false;
    }

}