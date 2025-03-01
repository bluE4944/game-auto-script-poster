package com.amc.poster.model;

import lombok.Data;

@Data
public class PosterInfo {

    // 应用相关参数
    private String app, version;
    private Integer x, y, runMaxTime;
    private String autoShutdown;

    // 面板1相关参数
    private String title, imgPath;
    private Double scaling;
    private Integer efficiency;

    // 面板2相关参数
    private String imagePath;

    // 脚本相关参数
    private Integer clickSpaceMin, clickSpaceMax, noActionMaxTime;

}
