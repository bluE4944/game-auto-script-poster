package com.amc.poster.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lsl
 * @since 2025/3/1 15:14
 * @version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "poster")
public class PosterProperties {
    /**
     * 应用名称
     */
    private String app;
    /**
     * 版本号
     */
    private String version;
    /**
     * 应用显示坐标-X
     */
    private Integer locationX;
    /**
     * 应用显示坐标-Y
     */
    private Integer locationY;
    /**
     * 应用最大运行时长, 单位: 秒
     * <p>该参数值必须大于599, 不然不会生效</p>
     */
    private String runMaxTime;
    /**
     * 达到最大运行时长后, 是否自动关机
     * <li><code>ON</code>[自动关机]</li>
     * <li><code>OFF</code>[关闭程序]</li>
     */
    private String autoShutdown;
    /**
     # 窗口标题
     */
    private String title;
    /**
     * 后台脚本的图片地址
     */
    private String backendImgPath;
    /**
     * 前台脚本的图片地址
     */
    private String fontImgPath;
    /**
     * 缩放比例
     */
    private Double scaling;
    /**
     * 脚本效率, 单位: 毫秒
     */
    private Integer efficiency;
    /**
     * 鼠标单击的最小耗时
     */
    private Integer clickSpaceMin;
    /**
     * 鼠标单击的最大耗时
     */
    private Integer clickSpaceMax;
    /**
     * 脚本没有动作的最大时长, 单位: 秒
     * 当脚本长时间没有操作时, 会播放音乐
     */
    private Integer noActionMaxTime = 300;

}
