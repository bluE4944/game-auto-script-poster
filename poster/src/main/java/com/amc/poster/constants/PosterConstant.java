package com.amc.poster.constants;

import com.amc.poster.model.PosterInfo;
import com.amc.util.file.PropertiesUtil;
import com.amc.util.file.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;

/**
 * @author AMC
 */
@Slf4j
public class PosterConstant {

    public static final String ICON_PATH = "/img/icon.png";
    public static String propertyPath;
    public static PosterInfo posterInfo;

    public static void loadData() {
        String configFilePath = "\\application-local.yml";
        initField(configFilePath);
//        checkConfigFile(configFilePath);
//        startCheckRunThread();
    }

    public static String getMusicPath() {
        return ResourceUtil.getResource("/default.wav");
    }

    private static void initField(String configFilePath) {
        String conf = ResourceUtil.bindResourceDir("conf");
        propertyPath = ResourceUtil.getResource(configFilePath);
//        posterInfo = PropertiesUtil.getModel(propertyPath, PosterInfo.class);
    }

    private static void checkConfigFile(String configFilePath) {
        // 检测并且自动更新配置文件
        try (InputStream ins = PosterConstant.class.getResourceAsStream(configFilePath)) {
            PosterInfo model = PropertiesUtil.getModel(ins, PosterInfo.class);
            if (model.getVersion().equals(posterInfo.getVersion())) {
                return;
            }
            if (new File(propertyPath).delete()) {
                propertyPath = ResourceUtil.getResource(configFilePath);
                posterInfo = PropertiesUtil.getModel(propertyPath, PosterInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }

    private static void startCheckRunThread() {
        Integer runMaxTime = posterInfo.getRunMaxTime();
        if (runMaxTime >= 600) {
            new Thread(() -> {
                try {
                    Thread.sleep(runMaxTime * 1000);

                    String autoShutdown = posterInfo.getAutoShutdown();
                    // 自动关机
                    if ("1".equals(autoShutdown) || "ON".equals(autoShutdown)) {
                        System.out.println("关闭电脑");
                        String command = "shutdown -s -t 60";
                        Runtime.getRuntime().exec(command);
                    }
                    // 自动关闭程序
                    if ("0".equals(autoShutdown) || "OFF".equals(autoShutdown)) {
                        System.out.println("关闭程序");
                        System.exit(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
