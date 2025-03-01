package com.amc.javafx.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 视图原生工具包
 */
public class ViewUtil {

    private static Map<String, ToggleGroup> toggleGroupMap;

    /**
     * 获取标签
     */
    public static Label getLabel(String title, double x, double y, double size) {
        Label label = new Label(title);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setFont(new Font(size));
        return label;
    }

    /**
     * 获取文本框
     */
    public static TextField getTextField(double x, double y, double width, double height) {
        TextField textField = new TextField();
        textField.setLayoutX(x);
        textField.setLayoutY(y);
        textField.setPrefSize(width, height);
        return textField;
    }

    /**
     * 获取数值框
     */
    public static TextField getNumberField(double x, double y, double width, double height) {
        TextField numberField = ViewUtil.getTextField(x, y, width, height);
        numberField.setTextFormatter(new TextFormatter<Integer>(change -> {
            String text = change.getText();
            return ("".equals(text) || text.matches("[-\\d]+")) ? change : null;
        }));
        return numberField;
    }

    /**
     * 获取密码框
     */
    public static PasswordField getPasswordField(double x, double y, double width, double height) {
        PasswordField passwordField = new PasswordField();
        passwordField.setLayoutX(x);
        passwordField.setLayoutY(y);
        passwordField.setPrefSize(width, height);
        return passwordField;
    }

    /**
     * 获取文本区
     */
    public static TextArea getTextArea(double x, double y, int column, int row) {
        TextArea textArea = new TextArea();
        textArea.setLayoutX(x);
        textArea.setLayoutY(y);
        textArea.setWrapText(true);
        textArea.setPrefColumnCount(column);
        textArea.setPrefRowCount(row);
        return textArea;
    }

    /**
     * 获取按钮
     */
    public static Button getButton(String title, double x, double y, double width, double height) {
        Button button = new Button(title);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefSize(width, height);
        return button;
    }

    /**
     * 获取单选按钮, 处于同一组的按钮只能选中一个
     */
    public static RadioButton getRadioButton(String title, double x, double y, double width, double height, String groupName) {
        RadioButton radioButton = new RadioButton(title);
        radioButton.setLayoutX(x);
        radioButton.setLayoutY(y);
        radioButton.setPrefSize(width, height);
        radioButton.setToggleGroup(getToggleGroup(groupName));
        return radioButton;
    }

    /**
     * 返回选中的单选按钮
     */
    public static RadioButton getSelectRadioButton(String groupName) {
        ToggleGroup toggleGroup = getToggleGroup(groupName);
        return (RadioButton) toggleGroup.getSelectedToggle();
    }

    /**
     * 获取多选按钮
     */
    public static CheckBox getCheckBox(String title, double x, double y, double width, double height) {
        CheckBox checkBox = new CheckBox(title);
        checkBox.setLayoutX(x);
        checkBox.setLayoutY(y);
        checkBox.setPrefSize(width, height);
        return checkBox;
    }

    /**
     * 获取滑动条
     */
    public static Slider getSlider(double min, double max, double defaultValue, double x, double y, double width, double height) {
        Slider slider = new Slider(min, max, defaultValue);
        slider.setLayoutX(x);
        slider.setLayoutY(y);
        slider.setPrefSize(width, height);
        slider.setSnapToTicks(false);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        return slider;
    }

    /**
     * 获取下拉列表
     */
    public static <T> ChoiceBox<T> getChoiceBox(double x, double y, double width, double height, List<T> values) {
        ChoiceBox<T> choiceBox = new ChoiceBox<>();
        choiceBox.setLayoutX(x);
        choiceBox.setLayoutY(y);
        choiceBox.setPrefSize(width, height);
        choiceBox.getItems().addAll(values);
        return choiceBox;
    }

    /**
     * 获取表格, 表格数据来源于tableView.getItems()
     * 对数据的增删会自动刷新UI, 对数据的修改需要手动调用tableView.refresh()
     */
    public static <T> TableView<T> getTableView(Class<T> dataClass, double x, double y, double width, double height) {
        ObservableList<T> data = FXCollections.observableArrayList();
        TableView<T> tableView = new TableView<>(data);
        tableView.setLayoutX(x);
        tableView.setLayoutY(y);
        tableView.setPrefSize(width, height);
        tableView.setTableMenuButtonVisible(true);
        ViewApi.initTableView(tableView, dataClass);
        return tableView;
    }

    /**
     * 获取空白图片框
     */
    public static ImageView getImageView(double x, double y, double width, double height) {
        ImageView imageView = new ImageView();
        imageView.setX(x);
        imageView.setY(y);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        return imageView;
    }

    /**
     * 获取本地图片
     */
    public static Image getImage(String imgFilePath) {
        String url = "file:/" + imgFilePath;
        return new Image(url);
    }

    /**
     * 获取缓存图片
     */
    public static Image getImage(BufferedImage buffImage) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(buffImage, "png", output);
            InputStream input = new ByteArrayInputStream(output.toByteArray());
            return new Image(input);
        } catch (IOException e) {
            throw new RuntimeException("无法转化图片:");
        }
    }

    /**
     * 获取空白视频框
     */
    public static MediaView getMediaView(double x, double y, double width, double height) {
        MediaView mediaView = new MediaView();
        mediaView.setLayoutX(x);
        mediaView.setLayoutY(y);
        mediaView.setFitWidth(width);
        mediaView.setFitHeight(height);
        mediaView.setPreserveRatio(false);
        return mediaView;
    }

    /**
     * 获取视频播放器
     */
    public static MediaPlayer getMediaPlayer(String url) {
        if (!url.startsWith("http")) {
            File file = new File(url);
            url = file.toURI().toString();
        }
        Media media = new Media(url);
        return new MediaPlayer(media);
    }

    /**
     * 获取菜单栏
     */
    public static MenuBar getMenuBar(double width, double height) {
        MenuBar menuBar = new MenuBar();
        menuBar.setPrefSize(width, height);
        return menuBar;
    }

    /**
     * 获取菜单
     */
    public static Menu getMenu(String title, MenuBar menuBar) {
        Menu menu = new Menu(title);
        menuBar.getMenus().add(menu);
        return menu;
    }

    /**
     * 获取菜单项
     */
    public static MenuItem getMenuItem(String title, Menu menu) {
        MenuItem menuItem = new MenuItem(title);
        menu.getItems().add(menuItem);
        return menuItem;
    }

    /**
     * 获取空白饼状图
     */
    public static PieChart getPieChart(String title, double x, double y, double width, double height) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle(title);
        pieChart.setLayoutX(x);
        pieChart.setLayoutY(y);
        pieChart.setPrefSize(width, height);
        pieChart.setAnimated(true);
        return pieChart;
    }

    /**
     * 获取空白柱状图
     */
    public static BarChart<String, Number> getBarChart(String title, double x, double y, double width, double height) {
        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle(title);
        barChart.setTitle(title);
        barChart.setLayoutX(x);
        barChart.setLayoutY(y);
        barChart.setPrefSize(width, height);
        barChart.setAnimated(true);
        return barChart;
    }

    /**
     * 获取空白折线图
     */
    public static LineChart<String, Number> getLineChart(String title, double x, double y, double width, double height) {
        LineChart<String, Number> lineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        lineChart.setTitle(title);
        lineChart.setLayoutX(x);
        lineChart.setLayoutY(y);
        lineChart.setPrefSize(width, height);
        lineChart.setAnimated(true);
        return lineChart;
    }

    /**
     * 获取切换面板
     */
    public static TabPane getTabPane(double width, double height) {
        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(width);
        tabPane.setPrefHeight(height);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    /**
     * 获取面板项
     */
    public static Tab getTab(String title, TabPane tabPane, Pane pane) {
        Tab tab = new Tab(title);
        tabPane.getTabs().add(tab);
        tab.setContent(pane);
        return tab;
    }

    /**
     * 获取绝对布局面板
     */
    public static AnchorPane getAnchorPane(double x, double y, double width, double height) {
        AnchorPane pane = new AnchorPane();
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setPrefSize(width, height);
        return pane;
    }

    /**
     * 获取流式布局面板
     */
    public static FlowPane getFlowPane(double x, double y, double width, double height) {
        FlowPane pane = new FlowPane();
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setPrefSize(width, height);
        return pane;
    }

    /**
     * 获取水平布局面板
     */
    public static HBox getHBox(double x, double y, double width, double height) {
        HBox box = new HBox();
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setPrefSize(width, height);
        return box;
    }

    /**
     * 获取垂直布局面板
     */
    public static VBox getVBox(double x, double y, double width, double height) {
        VBox box = new VBox();
        box.setLayoutX(x);
        box.setLayoutY(y);
        box.setPrefSize(width, height);
        return box;
    }

    /**
     * 弹出提示框
     */
    public static void showMessageDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("消息提示框");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * 弹出确认框
     */
    public static boolean showConfirmDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("选择确认框");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 弹出输入框
     */
    public static String showTextInputDialog(String msg) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("文本输入框");
        dialog.setHeaderText(null);
        dialog.setContentText(msg);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 弹出选择框
     */
    public static String showChoiceDialog(List<String> option) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(option.get(0), option);
        dialog.setTitle("快捷选择器");
        dialog.setHeaderText(null);
        dialog.setContentText("请选择:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 弹出文件夹选择框
     */
    public static File showDirectoryChooser() {
        String projectPath = System.getProperty("user.dir");
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("文件夹选择器");
        chooser.setInitialDirectory(new File(projectPath));
        return chooser.showDialog(new Stage());
    }

    /**
     * 获取按钮组
     */
    private static ToggleGroup getToggleGroup(String groupName) {
        if (toggleGroupMap == null) {
            toggleGroupMap = new HashMap<>();
        }
        ToggleGroup toggleGroup = toggleGroupMap.get(groupName);
        if (toggleGroup == null) {
            toggleGroup = new ToggleGroup();
            toggleGroupMap.put(groupName, toggleGroup);
        }
        return toggleGroup;
    }

}
