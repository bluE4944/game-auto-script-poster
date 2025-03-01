package com.amc.poster;

import com.amc.javafx.core.JavaFxApplication;
import com.amc.javafx.core.JavaFxSpringBootApplication;
import com.amc.poster.constants.PosterConstant;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author AMC
 */
@EnableAsync
@SpringBootApplication
public class PosterApplication {

    /**
     * 该程序需要以管理员身份运行
     */
    public static void main(String[] args) {
        try {
            // 加载常量数据
            PosterConstant.loadData();
        } catch (Exception e) {
            // 处理加载数据时可能出现的异常
            System.err.println("加载常量数据时出现错误: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 启动 JavaFX 应用
//        JavaFxApplication.run(PosterApplication.class, args);
        JavaFxSpringBootApplication.run(PosterApplication.class, args);
    }

}
