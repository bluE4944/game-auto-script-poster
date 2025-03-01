package com.amc.util.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Java项目打包后, 其配置文件也在Jar包中, 无法对其修改, 所以需要挂载
 */
public class ResourceUtil {

    private static String resourcePath;

    /**
     * 传入文件夹名, 将项目内部的resources挂载到该文件夹中
     */
    public static String bindResourceDir(String dirName) {
        String projectPath = System.getProperty("user.dir");
        resourcePath = projectPath + File.separator + dirName;
        File file = new File(resourcePath);
        if (!file.exists() && !file.mkdir()) {
            return null;
        }
        return resourcePath;
    }

    /**
     * 获取资源路径
     * @param path 示例: /application.yml
     */
    public static String getResource(String path) {
        String absolutePath = resourcePath + path;
        File file = new File(absolutePath);

        if (!file.exists()) {
            try (InputStream ins = ResourceUtil.class.getResourceAsStream(path);
                 OutputStream ous = Files.newOutputStream(Paths.get(absolutePath))) {
                int length;
                byte[] buf = new byte[1024];
                assert ins != null;
                while ((length = ins.read(buf)) > 0) {
                    ous.write(buf, 0, length);
                }
            } catch (Exception e) {
                throw new RuntimeException("拷贝文件失败");
            }
        }

        return absolutePath;
    }

}
