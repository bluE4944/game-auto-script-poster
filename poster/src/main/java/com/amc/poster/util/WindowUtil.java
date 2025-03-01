package com.amc.poster.util;

import com.sun.jna.Native;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.GDI32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 操作窗口的工具类
 * 该类的某些方法, 可能需要以管理员身份运行IDE才能调用成功
 */
public class WindowUtil {

    private static final MyUser32 user32 = MyUser32.INSTANCE;

    /**
     * 返回当前正在激活的窗口句柄
     */
    public static HWND getActiveHwnd() {
        return user32.GetForegroundWindow();
    }

    /**
     * 根据窗口标题返回窗口句柄
     */
    public static HWND getHwnd(String title) {
        return user32.FindWindow(null, title);
    }

    /**
     * 根据窗口标题前缀返回窗口句柄
     */
    public static HWND getHwndForPrefix(String titlePrefix) {
        for (DesktopWindow win : getAllWin()) {
            String title = win.getTitle();
            if (title.startsWith(titlePrefix)) {
                return win.getHWND();
            }
        }
        return null;
    }

    /**
     * 返回全部窗口信息
     */
    public static List<DesktopWindow> getAllWin() {
        return WindowUtils.getAllWindows(true)
                .stream()
                .filter(win -> !win.getTitle().equals(""))
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DesktopWindow::getTitle))), ArrayList::new));
    }

    /**
     * 返回窗口标题
     */
    public static String getWinTitle(HWND hwnd) {
        return WindowUtils.getWindowTitle(hwnd);
    }

    /**
     * 修改窗口标题
     */
    public static void setWinTitle(HWND hwnd, String title) {
        byte[] bytes = title.getBytes();
        char[] chars = new String(bytes, StandardCharsets.UTF_8).toCharArray();
        user32.SetWindowTextW(hwnd, chars);
    }

    /**
     * 返回窗口的坐标和尺寸
     */
    public static Rectangle getWinSize(HWND hwnd) {
        return WindowUtils.getWindowLocationAndSize(hwnd);
    }

    /**
     * 移动窗口
     */
    public static void moveWin(HWND hwnd, int x, int y, int w, int h) {
        // 设置指定窗口的显示状态
        user32.ShowWindow(hwnd, 1);
        // 激活指定窗口
        user32.SetForegroundWindow(hwnd);
        // 移动指定窗口的位置
        user32.MoveWindow(hwnd, x, y, w, h, true);
    }

    /**
     * 截取窗口, 窗口显示状态不能为最小化
     */
    public static BufferedImage getWinImage(HWND hwnd) {
        return GDI32Util.getScreenshot(hwnd);
    }

    /**
     * 向窗口发起点击单击鼠标事件
     */
    public static void mouseClick(HWND hwnd, int x, int y, long pressTime) {
        try {
            WinDef.LPARAM param = new WinDef.LPARAM(x + ((long) y << 16));
            user32.PostMessage(hwnd, 513, new WinDef.WPARAM(513), param);
            Thread.sleep(pressTime);
            user32.PostMessage(hwnd, 514, new WinDef.WPARAM(514), param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回上一次方法调用的错误码
     */
    public static int getLastError() {
        return Native.getLastError();
    }

    /**
     * 自定义扩展user32
     */
    interface MyUser32 extends User32 {

        MyUser32 INSTANCE = Native.load("user32", MyUser32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetWindowTextW(HWND hWnd, char[] lpString);

        boolean SetProcessDPIAware();

    }

}
