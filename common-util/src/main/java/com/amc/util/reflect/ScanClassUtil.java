package com.amc.util.reflect;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 扫描指定包下的类文件
 */
public class ScanClassUtil {

	private final static String FILE_PROTOCOL = "file";
	private final static String JAR_PROTOCOL = "jar";
	private final static String PACKAGE_SEPARATOR = ".";
	private final static String JAR_SEPARATOR = "!";
	private final static String FILE_SEPARATOR = "/";
	private final static String CLASS_SUFFIX = ".class";

	/**
	 * 获取指定包下的所有类名
	 */
	public static Set<String> scanPackage(String packageName) {
		String packagePath = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
		URL url = ClassLoader.getSystemClassLoader().getResource(packagePath);
		Set<String> result = new LinkedHashSet<>();
		if (Objects.isNull(url)) {
			return result;
		}
		String resourceProtocol = url.getProtocol();
		if (Objects.equals(resourceProtocol, FILE_PROTOCOL)) {
			File packageFile = new File(url.getPath());
			scanPackageClass(packageFile, packageName, result);
		}
		else if (Objects.equals(resourceProtocol, JAR_PROTOCOL)) {
			String jarPath = url.getPath();
			int beginIndex = jarPath.indexOf(FILE_SEPARATOR) + FILE_SEPARATOR.length();
			int endIndex = jarPath.indexOf(JAR_PROTOCOL + JAR_SEPARATOR) + JAR_PROTOCOL.length();
			jarPath = jarPath.substring(beginIndex, endIndex);
			scanJarPackageClassList(jarPath, packageName, result);
		}
		return result;
	}

	private static void scanPackageClass(File file, String packageName, Set<String> classNames) {
		if (file.isDirectory()) {
			for (File f : Objects.requireNonNull(file.listFiles())) {
				if (f.isDirectory()) {
					scanPackageClass(f, packageName, classNames);
				}
				else {
					String classAbsoluteFilePath = f.getAbsolutePath();
					if (classAbsoluteFilePath.endsWith(CLASS_SUFFIX)) {
						classAbsoluteFilePath = classAbsoluteFilePath.replace(File.separator, PACKAGE_SEPARATOR);
						int beginIndex = classAbsoluteFilePath.indexOf(packageName + PACKAGE_SEPARATOR);
						int endIndex = classAbsoluteFilePath.lastIndexOf(CLASS_SUFFIX);
						String className = classAbsoluteFilePath.substring(beginIndex, endIndex);
						// 排除匿名内部类产生的Class文件
						if (!className.matches(".+\\$\\d+$")) {
							classNames.add(className);
						}
					}
				}
			}
		}
	}

	private static void scanJarPackageClassList(String jarPath, String packageName, Set<String> classNames) {
		try {
			JarFile jar = new JarFile(jarPath);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				String name = jarEntry.getName().replace(FILE_SEPARATOR, PACKAGE_SEPARATOR);
				if (name.startsWith(packageName) && name.endsWith(CLASS_SUFFIX)) {
					int beginIndex = name.indexOf(packageName + PACKAGE_SEPARATOR);
					int endIndex = name.lastIndexOf(CLASS_SUFFIX);
					String className = name.substring(beginIndex, endIndex);
					classNames.add(className);
				}
			}
			jar.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
