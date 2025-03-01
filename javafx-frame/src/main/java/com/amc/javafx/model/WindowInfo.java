package com.amc.javafx.model;

import lombok.Data;

@Data
public class WindowInfo {

    /**
     * 窗口标题
     */
    private String title = "JavaFx";

    /**
     * 窗口坐标X
     */
    private Integer x = 0;

    /**
     * 窗口坐标Y
     */
    private Integer y = 0;

    /**
     * 窗口宽度
     */
    private Integer width = 300;

    /**
     * 窗口高度
     */
    private Integer height = 300;

    /**
     * 窗口图标
     */
    private String icon = null;

    /**
     * 是否支持尺寸缩放
     */
    private Boolean resizable = Boolean.FALSE;

    /**
     * 是否开启窗口置顶
     */
    private Boolean alwaysOnTop = Boolean.FALSE;

    /**
     * 窗口关闭后, 是否结束程序
     */
    private Boolean implicitExit = Boolean.TRUE;

}
