package com.amc.poster.gui;

import com.amc.javafx.annotations.Start;
import com.amc.javafx.interfaces.ViewBatchRegistry;
import com.amc.javafx.model.WindowInfo;
import com.amc.javafx.util.ViewsUtil;
import com.amc.poster.constants.PosterConstant;
import com.amc.poster.core.properties.PosterProperties;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class ViewConfig implements ViewBatchRegistry {

    private final PosterProperties posterProperties;

    @Start
    public void start(WindowInfo windowInfo) {
        windowInfo.setTitle(posterProperties.getApp());
        windowInfo.setX(posterProperties.getLocationX());
        windowInfo.setY(posterProperties.getLocationY());
        windowInfo.setWidth(489);
        windowInfo.setHeight(220);
        windowInfo.setIcon(PosterConstant.ICON_PATH);
    }

    @Override
    public Map<String, Node> getViews(Pane rootPane) {
        double labelFont = 18, labelX = 4.0;
        double textFieldX = 14.0, textFieldW = 55.0;
        double effTextLabelX = textFieldX + 26.6, effTextFieldX = effTextLabelX + textFieldX - labelX;
        double button1X = 71.0, button2X = 84.0;
        double line1Y = 10.0, line2Y = line1Y + 19, line3Y = line2Y + 19;
        double line4Y = line1Y + 9, line5Y = line4Y + 24;

        ViewsUtil viewsUtil = ViewsUtil.parent(rootPane)
                .anchorPane("pane1").width(100.0).height(100.0).create()
                .anchorPane("pane2").width(100.0).height(100.0).create()
                .tabPane("tabPane").width(100.0).height(100.0).create()
                .addTab("后台操作", "pane1")
                .addTab("前台操作", "pane2")
                .parent("pane1")
                .label("titleLabel").title("标题").font(labelFont).x(labelX).y(line1Y).create()
                .textField("titleTextField").x(textFieldX).y(line1Y - 1).width(textFieldW).create()
                .button("selectTitleButton").title("选择").x(button1X).y(line1Y - 1).create()
                .button("bindTitleButton").title("绑定").x(button2X).y(line1Y - 1).create()
                .label("imgPathLabel").title("图片").font(labelFont).x(labelX).y(line2Y).create()
                .textField("imgPathTextField").x(textFieldX).y(line2Y - 1).width(textFieldW).create()
                .button("selectImgPathButton").title("选择").x(button1X).y(line2Y - 1).create()
                .button("takeImgButton").title("拍照").x(button2X).y(line2Y - 1).create()
                .label("scalingLabel").title("缩放").font(labelFont).x(labelX).y(line3Y).create()
                .choiceBox("scalingChoiceBox", "1.0", "1.25", "1.5", "1.75")
                .x(textFieldX).y(line3Y - 1).width(textFieldW / 3).create()
                .label("efficiencyLabel").title("效率").font(labelFont).x(effTextLabelX).y(line3Y).create()
                .numberField("efficiencyTextField").x(effTextFieldX).y(line3Y - 1).width(textFieldW / 3).create()
                .button("startButton").title("开刷").x(button1X).y(line3Y - 1).create()
                .button("stopButton").title("停刷").x(button2X).y(line3Y - 1).create()
                .parent("pane2")
                .label("imagePathLabel").title("图片").font(labelFont).x(labelX).y(line4Y).create()
                .textField("imagePathTextField").x(textFieldX).y(line4Y - 1).width(textFieldW).create()
                .button("selectImagePathButton").title("选择").x(button1X).y(line4Y - 1).create()
                .button("takeImageButton").title("拍照").x(button2X).y(line4Y - 1).create()
                .button("startOrStopButton").title("开刷").x(42.5).y(line5Y).width(15.0).height(35).create();

        setViewText(viewsUtil);
        addPromptText(viewsUtil);
        return viewsUtil.end();
    }

    private void setViewText(ViewsUtil viewsUtil) {
        TextField titleTextField = viewsUtil.getNode("titleTextField", TextField.class);
        titleTextField.setText(posterProperties.getTitle());

        TextField imgPathTextField = viewsUtil.getNode("imgPathTextField", TextField.class);
        imgPathTextField.setText(posterProperties.getBackendImgPath());

        ChoiceBox<String> scalingChoiceBox = viewsUtil.getNode("scalingChoiceBox", ChoiceBox.class);
        scalingChoiceBox.setValue(posterProperties.getScaling().toString());

        TextField efficiencyTextField = viewsUtil.getNode("efficiencyTextField", TextField.class);
        efficiencyTextField.setText(posterProperties.getEfficiency().toString());

        TextField imagePathTextField = viewsUtil.getNode("imagePathTextField", TextField.class);
        imagePathTextField.setText(posterProperties.getFontImgPath());
    }

    /**
     * 为控件添加提示框, 鼠标短暂停留后弹出
     */
    private void addPromptText(ViewsUtil viewsUtil) {
        Label titleLabel = viewsUtil.getNode("titleLabel", Label.class);
        Tooltip.install(titleLabel, new Tooltip("获取方式: 请点击右边的选择或者绑定"));

        Button selectTitleButton = viewsUtil.getNode("selectTitleButton", Button.class);
        Tooltip.install(selectTitleButton, new Tooltip("选择某个你想操控的游戏窗口"));

        Button bindTitleButton = viewsUtil.getNode("bindTitleButton", Button.class);
        Tooltip.install(bindTitleButton, new Tooltip("请在规定时间内, 用鼠标点击你想操控的游戏窗口"));

        Label imgPathLabel = viewsUtil.getNode("imgPathLabel", Label.class);
        Tooltip.install(imgPathLabel, new Tooltip("请点击右边的选择, 选出存储图片的文件夹"));

        Button selectImgPathButton = viewsUtil.getNode("selectImgPathButton", Button.class);
        Tooltip.install(selectImgPathButton, new Tooltip("选择存储图片的文件夹, 然后点击拍照完成截图"));

        Button takeImgButton = viewsUtil.getNode("takeImgButton", Button.class);
        String buf1 = "拍照后会弹出窗口截图\n" +
                "你需要拖动鼠标, 裁剪至少一张图片\n" +
                "脚本会定期扫描游戏窗口, 如果发现了[视图区域], 则会进行模拟点击\n" +
                "脚本会随机选择某个[点击区域], 然后对该区域进行点击\n" +
                "如果只截取了[视图区域], 则只会点击该区域\n" +
                "截取图片完毕后, 请关闭弹窗, 它才会保存你截取的图片";
        Tooltip.install(takeImgButton, new Tooltip(buf1));

        Label scalingLabel = viewsUtil.getNode("scalingLabel", Label.class);
        String buf2 = "电脑的系统缩放比例, 设置方式如下\n" +
                "点击拍照后, 如果出现黑边, 则设为你电脑的系统缩放比例, 一般是1.25\n" +
                "如果没有出现黑边, 则设为1.0\n" +
                "即便如此, 还是可能出问题, 建议把电脑缩放比例调到100%";
        Tooltip.install(scalingLabel, new Tooltip(buf2));

        Label efficiencyLabel = viewsUtil.getNode("efficiencyLabel", Label.class);
        Tooltip.install(efficiencyLabel, new Tooltip("脚本扫描窗口的效率, 默认1000ms, 即每秒扫描一次"));

        Button startButton = viewsUtil.getNode("startButton", Button.class);
        Tooltip.install(startButton, new Tooltip("启动一个脚本\n它会根据文件夹里面的图片, 定期扫描并操作指定的游戏窗口\n每点击一次就会启动一个全新的脚本"));

        Button stopButton = viewsUtil.getNode("stopButton", Button.class);
        Tooltip.install(stopButton, new Tooltip("关闭之前启动的所有脚本"));

        Label imagePathLabel = viewsUtil.getNode("imagePathLabel", Label.class);
        Tooltip.install(imagePathLabel, new Tooltip("请点击右边的选择, 选出存储图片的文件夹"));

        Button selectImagePathButton = viewsUtil.getNode("selectImagePathButton", Button.class);
        Tooltip.install(selectImagePathButton, new Tooltip("选择存储图片的文件夹, 然后点击拍照完成截图"));

        Button takeImageButton = viewsUtil.getNode("takeImageButton", Button.class);
        String buf3 = "拍照后会弹出桌面截图\n" +
                "你需要拖动鼠标, 裁剪至少一张图片\n" +
                "脚本会定期扫描整个桌面, 如果发现了[视图区域], 则会进行鼠标点击\n" +
                "脚本会随机选择某个[点击区域], 然后对该区域进行点击\n" +
                "如果只截取了[视图区域], 则只会点击该区域\n" +
                "截取图片完毕后, 请关闭弹窗, 它才会保存你截取的图片";
        Tooltip.install(takeImageButton, new Tooltip(buf3));
    }

}
