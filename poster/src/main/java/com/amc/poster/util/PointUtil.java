package com.amc.poster.util;

import java.awt.*;
import java.util.ArrayList;

/**
 * 解析坐标点工具类
 */
public class PointUtil {

    public static String getString(ArrayList<Rectangle> rectangles) {
        StringBuilder buf = new StringBuilder();
        for (Rectangle rectangle : rectangles) {
            long p = rectangle.x + ((long) rectangle.y << 16);
            long d = rectangle.width + ((long) rectangle.height << 16);
            buf.append("$").append(p).append(".").append(d);
        }
        return buf.toString();
    }

    public static ArrayList<Rectangle> getList(String str) {
        ArrayList<Rectangle> result = new ArrayList<>();

        for (String s : str.split("\\$")) {
            String[] pd = s.split("\\.");
            if (pd.length == 2) {
                long p = Long.parseLong(pd[0]);
                long d = Long.parseLong(pd[1]);
                int x = (int) p & 65535;
                int y = (int) p >> 16;
                int w = (int) d & 65535;
                int h = (int) d >> 16;
                result.add(new Rectangle(x, y, w, h));
            }
        }

        return result;
    }

}
