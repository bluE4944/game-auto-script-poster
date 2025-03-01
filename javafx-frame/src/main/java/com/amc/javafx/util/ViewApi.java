package com.amc.javafx.util;

import com.amc.util.reflect.ReflectUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 视图增强工具包
 */
public class ViewApi {

    /**
     * 获取显示滑动条值的标签, 它始终位于滑片正上方
     */
    public static Label getSliderValueLabel(Slider slider, double top) {
        double maxValue = slider.getMax(), sliderWidth = slider.getPrefWidth();
        double sliderX = slider.getLayoutX() + slider.getTranslateX();
        double sliderY = slider.getLayoutY() + slider.getTranslateY();
        double thumbSize = 20;

        Label label = new Label();
        label.setLayoutY(sliderY - top);
        label.setFont(new Font(18));

        NumberStringConverter converter = new NumberStringConverter("#0.0");
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            label.setText(converter.toString(newValue));
            double percent = newValue.doubleValue() / maxValue;
            double thumbX = sliderX + percent * sliderWidth;
            double thumbCenterX = thumbX - thumbSize * (percent - 0.5);
            double labelX = thumbCenterX - label.getWidth() / 2;
            label.setLayoutX(labelX);
        });
        slider.setOnMouseReleased(event -> {
            label.setText("");
        });
        return label;
    }

    /**
     * 获取饼状图相关数据
     */
    public static Map<String, Double> getPieChartDataMap(PieChart pieChart) {
        ObservableList<PieChart.Data> dataList = pieChart.getData();
        return new LinkedHashMap<String, Double>() {
            @Override
            public Double put(String key, Double value) {
                Double putResult = super.put(key, value);
                if (putResult == null) {
                    dataList.add(new PieChart.Data(key, value));
                } else {
                    for (PieChart.Data data : dataList) {
                        if (data.getName().equals(key)) {
                            data.setPieValue(value);
                            break;
                        }
                    }
                }
                double sum = this.values().stream().mapToDouble(Double::doubleValue).sum();
                dataList.forEach(data -> {
                    double val = data.getPieValue();
                    int percent = (int) (val / sum * 100);
                    Tooltip.install(data.getNode(), new Tooltip(val + " - " + percent + "%"));
                });
                return putResult;
            }
            @Override
            public Double remove(Object key) {
                Double removeResult = super.remove(key);
                for (PieChart.Data data : dataList) {
                    if (data.getName().equals(key)) {
                        dataList.remove(data);
                        break;
                    }
                }
                return removeResult;
            }
        };
    }

    /**
     * 获取[柱状图/折线图]相关数据
     */
    public static Map<String, Map<String, Number>> getXyChartDataMap(XYChart<String, Number> xyChart, List<String> keyNames, List<String> valNames) {
        Map<String, Map<String, Number>> result = new LinkedHashMap<String, Map<String, Number>>() {
            @Override
            public Map<String, Number> get(Object key) {
                if (!keyNames.contains(key)) {
                    throw new RuntimeException("Key取值: " + keyNames);
                }
                return super.get(key);
            }
        };
        for (String keyName : keyNames) {
            XYChart.Series<String, Number> axisXY = new XYChart.Series<>();
            axisXY.setName(keyName);
            xyChart.getData().add(axisXY);

            Map<String, Number> map = new LinkedHashMap<String, Number>() {
                @Override
                public Number put(String key, Number value) {
                    if (!valNames.contains(key)) {
                        throw new RuntimeException("Key取值: " + valNames);
                    }
                    Number putResult = super.put(key, value);
                    if (putResult == null) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>(key, value);
                        axisXY.getData().add(data);
                        Tooltip.install(data.getNode(), new Tooltip(value.toString()));
                    } else {
                        for (XYChart.Data<String, Number> data : axisXY.getData()) {
                            if (data.getXValue().equals(key)) {
                                data.setYValue(value);
                                Tooltip.install(data.getNode(), new Tooltip(value.toString()));
                                break;
                            }
                        }
                    }
                    return putResult;
                }
            };
            for (String valName : valNames) {
                map.put(valName, 0);
            }
            result.put(keyName, map);
        }
        return result;
    }

    /**
     * 初始化TableView, 自动执行通用操作
     */
    protected static <T> void initTableView(TableView<T> tableView, Class<T> dataClass) {
        List<Field> fields = ReflectUtil.getFields(dataClass);
        for (Field field : fields) {
            String title = field.getName();
            Resource resourceAnnotation = field.getAnnotation(Resource.class);
            if (resourceAnnotation != null) {
                String name = resourceAnnotation.name();
                if (!"".equals(name)) title = name;
            }
            double columnWidth = tableView.getPrefWidth() / fields.size();

            TableColumn<T, String> tableColumn = new TableColumn<>(title);
            tableColumn.setPrefWidth(columnWidth);
            tableView.getColumns().add(tableColumn);

            // 更新数据
            tableColumn.setCellValueFactory(param -> {
                T data = param.getValue();
                Object fieldValue = ReflectUtil.getFieldValue(data, field);
                String columnText = fieldValue != null ? fieldValue.toString() : "";
                return new SimpleStringProperty(columnText);
            });
            // 字段居中
            tableColumn.setCellFactory(param -> {
                TableCell<T, String> data = new TableCell<T, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (item != null) {
                            setText(item);
                        }
                    }
                };
                data.setAlignment(Pos.CENTER);
                return data;
            });
        }
    }

    /**
     * 获取视频通用控件面板, 它始终位于视频正下方
     */
    public static Pane getMediaViewButtonPane(MediaView mediaView, List<String> urls, int skipSecond) {
        double mediaX = mediaView.getLayoutX(), mediaY = mediaView.getLayoutY();
        double mediaW = mediaView.getFitWidth(), mediaH = mediaView.getFitHeight();

        String timeLabelText = "00:00:00";
        double timeLabelY = 0, timeLabelW = 60D, timeLabelH = 18D;
        double timeSliderW = mediaW - 2 * timeLabelW;
        double endTimeLabelX = timeLabelW + timeSliderW;

        Label currentTimeLabel = ViewUtil.getLabel(timeLabelText, 0, timeLabelY, -1);
        currentTimeLabel.setPrefSize(timeLabelW, timeLabelH);

        Slider timeSlider = new Slider();
        timeSlider.setLayoutX(timeLabelW);
        timeSlider.setLayoutY(timeLabelY);
        timeSlider.setPrefSize(timeSliderW, timeLabelH);
        timeSlider.setSnapToTicks(false);
        timeSlider.setFocusTraversable(true);

        Label endTimeLabel = ViewUtil.getLabel(timeLabelText, endTimeLabelX, timeLabelY, -1);
        endTimeLabel.setPrefSize(timeLabelW, timeLabelH);

        double buttonY = timeLabelY + 25, buttonSize = 40, buttonSpace = 10;
        double playButtonX = mediaW / 2 - buttonSize / 2;
        double lastButtonX = playButtonX - buttonSize - buttonSpace;
        double nextButtonX = playButtonX + buttonSize + buttonSpace;
        double volumeButtonX = lastButtonX - buttonSize - buttonSpace;
        double rateButtonX = nextButtonX + buttonSize + buttonSpace;

        Button playButton = ViewUtil.getButton("▶", playButtonX, buttonY, buttonSize, buttonSize);
        Button lastButton = ViewUtil.getButton("⏪", lastButtonX, buttonY, buttonSize, buttonSize);
        Button nextButton = ViewUtil.getButton("⏩", nextButtonX, buttonY, buttonSize, buttonSize);
        Button volumeButton = ViewUtil.getButton("\uD83D\uDD0A", volumeButtonX, buttonY, buttonSize, buttonSize);
        Button rateButton = ViewUtil.getButton("速", rateButtonX, buttonY, buttonSize, buttonSize);

        String style = "-fx-border-radius: 50; -fx-background-radius:50";
        playButton.setStyle(style);
        lastButton.setStyle(style);
        nextButton.setStyle(style);
        volumeButton.setStyle(style);
        rateButton.setStyle(style);

        Slider volumeSlider = new Slider(0, 100, 100);
        volumeSlider.setLayoutX(-45);
        volumeSlider.setPrefSize(90, 18);
        volumeSlider.setTranslateX(22.5);
        volumeSlider.setTranslateY(52);
        volumeSlider.setRotate(-90);

        Label volumeLabel = ViewUtil.getLabel("100", 11.5, 0, 13);
        volumeLabel.setTextFill(Color.WHITE);

        double volumePaneW = 45, volumePaneH = 110;
        double volumePaneX = volumeButtonX + (buttonSize - volumePaneW) / 2;
        double volumePaneY = buttonY - volumePaneH - 5;

        Pane volumePane = ViewUtil.getAnchorPane(volumePaneX, volumePaneY, volumePaneW, volumePaneH);
        volumePane.setStyle("-fx-background-color: DimGray; -fx-border-radius: 12; -fx-background-radius:12");
        volumePane.getChildren().addAll(volumeSlider, volumeLabel);

        Slider rateSlider = new Slider(0.5, 2.0, 1.0);
        rateSlider.setLayoutX(-45);
        rateSlider.setPrefSize(90, 18);
        rateSlider.setTranslateX(22.5);
        rateSlider.setTranslateY(52);
        rateSlider.setRotate(-90);

        Label rateLabel = ViewUtil.getLabel("1.0", 13.5, 0, 13);
        rateLabel.setTextFill(Color.WHITE);

        double ratePaneX = rateButtonX + (buttonSize - volumePaneW) / 2;

        Pane ratePane = ViewUtil.getAnchorPane(ratePaneX, volumePaneY, volumePaneW, volumePaneH);
        ratePane.setStyle("-fx-background-color: DimGray; -fx-border-radius: 12; -fx-background-radius:12");
        ratePane.getChildren().addAll(rateSlider, rateLabel);

        initMediaPlayer(mediaView, urls, skipSecond,
                timeSlider, currentTimeLabel, endTimeLabel,
                playButton, lastButton, nextButton, volumeButton, rateButton,
                volumePane, volumeSlider, volumeLabel, ratePane, rateSlider, rateLabel);

        Pane pane = new AnchorPane();
        pane.setLayoutX(mediaX);
        pane.setLayoutY(mediaY + mediaH);
        pane.setPrefSize(mediaW, 70);
        pane.getChildren().addAll(timeSlider, currentTimeLabel, endTimeLabel);
        pane.getChildren().addAll(playButton, lastButton, nextButton, volumeButton, rateButton);
        pane.getChildren().addAll(volumePane, ratePane);
        return pane;
    }

    private static void initMediaPlayer(MediaView mediaView, List<String> urls, int skipSecond,
                                        Slider timeSlider, Label currentTimeLabel, Label endTimeLabel,
                                        Button playButton, Button lastButton, Button nextButton, Button volumeButton, Button rateButton,
                                        Pane volumePane, Slider volumeSlider, Label volumeLabel, Pane ratePane, Slider rateSlider, Label rateLabel) {
        // 每个视频文件都需要创建新的MediaPlayer, 该对象是无法复用的
        // 每次播放新视频的时候, 都需要重新设置监听器
        // 其他元素的监听器虽然不需要重新设置, 但监听的是老的MediaPlayer, 因此采用以下方式莱解决此问题
        AtomicReference<MediaPlayer> mediaPlayerObject = new AtomicReference<>();
        AtomicInteger urlIndex = new AtomicInteger(0);
        AtomicBoolean showVolumePane = new AtomicBoolean(false);
        AtomicBoolean showRatePane = new AtomicBoolean(false);
        AtomicBoolean showFullScreen = new AtomicBoolean(false);

        createMediaPlayer(mediaView, urls, mediaPlayerObject, urlIndex,
                timeSlider, currentTimeLabel, endTimeLabel, playButton, volumeSlider, rateSlider);
        volumePane.setVisible(showVolumePane.get());
        ratePane.setVisible(showRatePane.get());

        // 拖动进度条触发以下事件
        timeSlider.setOnMousePressed(event -> {
            mediaPlayerObject.get().pause();
        });
        timeSlider.setOnMouseDragged(event -> {
            mediaPlayerObject.get().seek(Duration.seconds(timeSlider.getValue()));
        });
        timeSlider.setOnMouseReleased(event -> {
            MediaPlayer mediaPlayer = mediaPlayerObject.get();
            mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            mediaPlayer.play();
        });
        timeSlider.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                mediaPlayerObject.get().pause();
            }
        });
        timeSlider.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                MediaPlayer mediaPlayer = mediaPlayerObject.get();
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                mediaPlayer.play();
            }
        });
        // 播放与暂停
        playButton.setOnAction(event -> {
            MediaPlayer mediaPlayer = mediaPlayerObject.get();
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        });
        // 后退N秒与播放上一集
        lastButton.setOnMouseClicked(event -> {
            MediaPlayer mediaPlayer = mediaPlayerObject.get();
            int clickCount = event.getClickCount();
            if (clickCount == 1) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                mediaPlayer.seek(Duration.seconds(currentTime - skipSecond));
            }
            else if (clickCount == 2) {
                int index = urlIndex.get();
                if (index == 0) return;
                urlIndex.set(index - 1);
                mediaPlayer.dispose();
                createMediaPlayer(mediaView, urls, mediaPlayerObject, urlIndex,
                        timeSlider, currentTimeLabel, endTimeLabel, playButton, volumeSlider, rateSlider);
            }
        });
        // 前进N秒与播放下一集
        nextButton.setOnMouseClicked(event -> {
            MediaPlayer mediaPlayer = mediaPlayerObject.get();
            int clickCount = event.getClickCount();
            if (clickCount == 1) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                mediaPlayer.seek(Duration.seconds(currentTime + skipSecond));
            }
            else if (clickCount == 2) {
                int index = urlIndex.get();
                int nextIndex = index + 1;
                if (nextIndex == urls.size()) nextIndex = 0;
                urlIndex.set(nextIndex);
                mediaPlayer.dispose();
                createMediaPlayer(mediaView, urls, mediaPlayerObject, urlIndex,
                        timeSlider, currentTimeLabel, endTimeLabel, playButton, volumeSlider, rateSlider);
            }
        });
        // 音量面板的显示与隐藏
        volumeButton.setOnAction(event -> {
            boolean newVal = !showVolumePane.get();
            showVolumePane.set(newVal);
            volumePane.setVisible(newVal);
        });
        // 调整视频音量
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double val = newValue.doubleValue();
            mediaPlayerObject.get().setVolume(val / 100);
            int value = (int) val;
            volumeLabel.setText(String.valueOf(value));
            if (value < 10) volumeLabel.setLayoutX(19.5);
            else if (value < 100) volumeLabel.setLayoutX(15.5);
            else volumeLabel.setLayoutX(11.5);
        });
        // 速率面板的显示与隐藏
        rateButton.setOnAction(event -> {
            boolean newVal = !showRatePane.get();
            showRatePane.set(newVal);
            ratePane.setVisible(newVal);
        });
        // 调整视频速率
        DecimalFormat df = new DecimalFormat("0.0");
        rateSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double rate = newValue.doubleValue();
            mediaPlayerObject.get().setRate(rate);
            rateLabel.setText(df.format(rate));
        });
        // 点击视频画面会触发以下事件
        mediaView.setOnMouseClicked(event -> {
            if (showFullScreen.get()) return;
            int clickCount = event.getClickCount();
            if (clickCount == 1) {
                playButton.fire();
            }
            else if (clickCount == 2) {
                mediaPlayerObject.get().play();
                startFullScreen(mediaView, showFullScreen, timeSlider, playButton, volumeSlider);
            }
        });
    }

    private static void createMediaPlayer(MediaView mediaView, List<String> urls,
                                          AtomicReference<MediaPlayer> mediaPlayerObject, AtomicInteger urlIndex,
                                          Slider timeSlider, Label currentTimeLabel, Label endTimeLabel,
                                          Button playButton, Slider volumeSlider, Slider rateSlider) {
        String url = urls.get(urlIndex.get());
        MediaPlayer mediaPlayer = ViewUtil.getMediaPlayer(url);
        mediaPlayer.setAutoPlay(true);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayerObject.set(mediaPlayer);

        // 视频生命周期方法
        mediaPlayer.setOnReady(() -> {
            double startTime = mediaPlayer.getStartTime().toSeconds();
            double mediaTime = mediaPlayer.getTotalDuration().toSeconds();
            timeSlider.setMin(0);
            timeSlider.setMax(mediaTime);
            timeSlider.setValue(startTime);
            endTimeLabel.setText(formatSecond(mediaTime));
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            mediaPlayer.setRate(rateSlider.getValue());
        });
        mediaPlayer.setOnPlaying(() -> {
            playButton.setText("⏸");
        });
        mediaPlayer.setOnPaused(() -> {
            playButton.setText("▶");
        });
        mediaPlayer.setOnStopped(() -> {
            playButton.setText("▶");
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.stop();
        });
        // 进度条随着播放时间移动
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double seconds = newValue.toSeconds();
            timeSlider.setValue(seconds);
            currentTimeLabel.setText(formatSecond(seconds));
        });
    }

    private static String formatSecond(Double second) {
        int seconds = second.intValue();
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private static void startFullScreen(MediaView mediaView, AtomicBoolean showFullScreen,
                                        Slider timeSlider, Button playButton, Slider volumeSlider) {
        showFullScreen.set(true);
        double volumeCount = volumeSlider.getMax() / 10;

        Pane mvPane = (Pane) mediaView.getParent();
        Pane tsPane = (Pane) timeSlider.getParent();

        double mvx = mediaView.getLayoutX(), mvy = mediaView.getLayoutY();
        double mvw = mediaView.getFitWidth(), mvh = mediaView.getFitHeight();
        double tsx = timeSlider.getLayoutX(), tsy = timeSlider.getLayoutY();
        double tsw = timeSlider.getWidth(), tsh = timeSlider.getHeight();

        Label tooltipLabel = ViewUtil.getLabel("", 0, 0, 18);
        tooltipLabel.setTextFill(Color.WHITE);
        tooltipLabel.setStyle("-fx-background-color:black;");

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(mediaView, timeSlider, tooltipLabel);
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.show();

        double winW = root.getWidth(), winH = root.getHeight();
        mediaView.setLayoutX(0);
        mediaView.setLayoutY(0);
        mediaView.setFitWidth(winW);
        mediaView.setFitHeight(winH);

        timeSlider.setLayoutX(0);
        timeSlider.setLayoutY(winH - tsh / 2);
        timeSlider.setPrefWidth(winW);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.ESCAPE) {
                showFullScreen.set(false);

                mediaView.setLayoutX(mvx);
                mediaView.setLayoutY(mvy);
                mediaView.setFitWidth(mvw);
                mediaView.setFitHeight(mvh);
                mvPane.getChildren().add(mediaView);
                mediaView.toBack();

                timeSlider.setLayoutX(tsx);
                timeSlider.setLayoutY(tsy);
                timeSlider.setPrefSize(tsw, tsh);
                tsPane.getChildren().add(timeSlider);
                timeSlider.toBack();

                stage.close();
            }
            else if (code == KeyCode.SPACE) {
                playButton.fire();
            }
            else if (code == KeyCode.UP || code == KeyCode.DOWN) {
                double value = volumeSlider.getValue();
                value = (code == KeyCode.UP) ? value + volumeCount : value - volumeCount;
                volumeSlider.setValue(value);
                int val = (int) volumeSlider.getValue();
                tooltipLabel.setText("音量调整: " + val);
                tooltipLabel.setVisible(true);
                new Timeline(new KeyFrame(Duration.seconds(2), e -> tooltipLabel.setVisible(false))).play();
            }
        });
    }

}
