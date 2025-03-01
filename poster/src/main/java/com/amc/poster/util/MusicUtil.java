package com.amc.poster.util;

import lombok.RequiredArgsConstructor;

import javax.media.*;
import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * 音乐工具类
 */
public class MusicUtil {

    /**
     * 获取播放器
     */
    public static Player getPlayer(String wavPath) {
        try {
            URL url = new File(wavPath).toURI().toURL();
            Player player = Manager.createPlayer(url);
            player.addControllerListener(new MusicListener(player));
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("音乐播放器加载失败");
        }
    }

    /**
     * 开始播放 / 继续播放
     */
    public static void start(Player player) {
        player.start();
    }

    /**
     * 暂停播放
     */
    public static void stop(Player player) {
        player.stop();
    }

    /**
     * 跳转播放位置
     */
    public static void skip(Player player, double startTime) {
        if (Objects.equals(player.getState(), Player.Started)) {
            player.stop();
        }
        player.setMediaTime(new Time(startTime));
        player.start();
    }

    /**
     * 设置音量
     */
    public static void setVolume(Player player, int level) {
        if (level < 0) level = 0;
        if (level > 8) level = 8;
        float volume = (float) (level / 10.0);
        player.getGainControl().setLevel(volume);
    }

    /**
     * 获取音乐时长
     */
    public static double getMusicTime(Player player) {
        return player.getDuration().getSeconds();
    }

    @RequiredArgsConstructor
    static class MusicListener extends ControllerAdapter {
        private final Player player;

        @Override
        public void endOfMedia(EndOfMediaEvent e) {
            super.endOfMedia(e);
            player.close();
        }

    }

}
