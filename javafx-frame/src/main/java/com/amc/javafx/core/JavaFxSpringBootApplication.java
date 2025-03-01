package com.amc.javafx.core;

import com.amc.javafx.constants.FxConstant;
import com.amc.javafx.model.WindowInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

/**
 * java fx spring boot application
 * @author lsl
 * @since 2025年3月1日 13点14分
 * @version 1.0
 */
public class JavaFxSpringBootApplication extends Application {

    private static Class<?> mainClass;
    private ConfigurableApplicationContext applicationContext;

    public static void run(Class<?> configClass, String[] args) {
        mainClass = configClass;
        launch(args);
    }

    @Override
    public void init() {
        // 启动 Spring Boot 应用
        String[] args = getParameters().getRaw().toArray(new String[0]);
        applicationContext = new SpringApplicationBuilder(mainClass).run(args);
    }

    @Override
    public void start(Stage window) {
        Pane rootPane = new AnchorPane();

        // 将 Stage 和 rootPane 注册到 Spring 上下文中
        applicationContext.getBeanFactory().registerSingleton(FxConstant.STAGE, window);
        applicationContext.getBeanFactory().registerSingleton(FxConstant.ROOT_PANE, rootPane);

        WindowInfo windowInfo = applicationContext.getBean(WindowInfo.class);
        setWindow(window, windowInfo);

        Scene scene = new Scene(rootPane);
        window.setScene(scene);
        window.show();
    }

    private void setWindow(Stage window, WindowInfo windowInfo) {
        window.setTitle(windowInfo.getTitle());
        window.setX(windowInfo.getX());
        window.setY(windowInfo.getY());
        window.setWidth(windowInfo.getWidth());
        window.setHeight(windowInfo.getHeight());
        if (Objects.nonNull(windowInfo.getIcon())) {
            window.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(windowInfo.getIcon()))));
        }
        window.setResizable(windowInfo.getResizable());
        window.setAlwaysOnTop(windowInfo.getAlwaysOnTop());
        Platform.setImplicitExit(windowInfo.getImplicitExit());
    }

    @Override
    public void stop() {
        applicationContext.getBean(ViewFactory.class).doStop();
        applicationContext.close(); // 关闭 Spring Boot 应用上下文
    }
}
