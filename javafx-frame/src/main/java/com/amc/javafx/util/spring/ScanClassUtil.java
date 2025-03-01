package com.amc.javafx.util.spring;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 基于spring的扫描工具
 */
public class ScanClassUtil {

    private final static String PACKAGE_SEPARATOR = ".";
    private final static String FILE_SEPARATOR = "/";
    private final static String CLASS_SUFFIX = ".class";

    public static Set<String> scanPackage(String packageName) {
        Set<String> result = new LinkedHashSet<>();
        try {
            String packagePath = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
            String packageSearchPath = "classpath*:" + packagePath + "/**/*.class";
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(packageSearchPath);
            for (Resource resource : resources) {
                String classAbsoluteFilePath = resource.getURL().getPath();
                int beginIndex = classAbsoluteFilePath.indexOf(packagePath);
                int endIndex = classAbsoluteFilePath.lastIndexOf(CLASS_SUFFIX);
                String className = classAbsoluteFilePath.substring(beginIndex, endIndex);
                className = className.replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
                if (!className.matches(".+\\$\\d+$")) {
                    result.add(className);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("扫描类文件失败");
        }
        return result;
    }

}
