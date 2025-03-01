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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;

public class JavaFxApplication extends Application {

    private static Class<?> mainClass;
    private ApplicationContext applicationContext;

    public static void run(Class<?> configClass, String[] args) {
        mainClass = configClass;

        launch(args);
    }

    @Override
    public void start(Stage window) {
        Pane rootPane = new AnchorPane();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(FxFrameConfiguration.class, mainClass);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        beanFactory.registerSingleton(FxConstant.STAGE, window);
        beanFactory.registerSingleton(FxConstant.ROOT_PANE, rootPane);
        applicationContext.refresh();
        this.applicationContext = applicationContext;

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
            window.getIcons().add(new Image(getClass().getResourceAsStream(windowInfo.getIcon())));
        }
        window.setResizable(windowInfo.getResizable());
        window.setAlwaysOnTop(windowInfo.getAlwaysOnTop());
        Platform.setImplicitExit(windowInfo.getImplicitExit());
    }

    @Override
    public void stop() {
        applicationContext.getBean(ViewFactory.class).doStop();
    }

}
