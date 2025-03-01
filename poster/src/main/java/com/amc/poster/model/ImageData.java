package com.amc.poster.model;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {

    /**
     * 图片文件名
     */
    private String fileName;

    /**
     * 图片颜色数据
     */
    private int[][] rgbData;

    /**
     * 坐标数据
     */
    private ArrayList<Rectangle> rects;

    /**
     * 获取图片在大图中的坐标
     */
    public Rectangle getImgRect() {
        if (CollectionUtil.isEmpty(rects)) {
            return null;
        }
        return rects.get(0);
    }

    /**
     * 获取随机点击区域
     */
    public Rectangle getPoint() {
        int size = rects.size();
        if (size == 1) {
            return rects.get(0);
        }
        int index = new Random().nextInt(size - 1) + 1;
        return rects.get(index);
    }

}
