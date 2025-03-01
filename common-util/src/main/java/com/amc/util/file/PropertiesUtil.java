package com.amc.util.file;

import com.amc.util.reflect.ReflectUtil;
import com.amc.util.reflect.core.TypeParseUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java Properties不好用, 所以重写了
 */
public class PropertiesUtil {

    /**
     * 读取指定文件, 将其内容解析为对象
     */
    public static <T> T getModel(String filePath, Class<T> modelClass) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            return getModel(inputStream, modelClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 读取指定文件流, 将其内容解析为对象
     */
    public static <T> T getModel(InputStream inputStream, Class<T> modelClass) {
        List<FieldInfo> fieldInfos = readInputStream(inputStream);
        Map<String, String> map = fieldInfos.stream().collect(Collectors.toMap(FieldInfo::getName, FieldInfo::getValue));

        T bean = ReflectUtil.newInstance(modelClass);
        for (Field field : ReflectUtil.getFields(modelClass)) {
            String value = map.get(field.getName());
            if (Objects.nonNull(value)) {
                try {
                    Object parseVal = TypeParseUtil.parse(value, field.getType());
                    ReflectUtil.setFieldValue(bean, field, parseVal);
                } catch (Exception e) {
                    System.err.println("err: " + e.getMessage());
                }
            }
        }
        return bean;
    }

    public  static <T> T getYmlModel(String filePath, Class<T> modelClass) {

    }

    /**
     * 将对象以键值对的方式写入到文件中
     */
    public static void saveModel(String filePath, Object modelObject) {
        Map<String, String> map = null;
        if (new File(filePath).exists()) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
                List<FieldInfo> fieldInfos = readInputStream(inputStream);
                map = fieldInfos.stream().collect(Collectors.toMap(FieldInfo::getName, FieldInfo::getNote));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(Paths.get(filePath)), StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            Class<?> modelClass = modelObject.getClass();
            for (Field field : ReflectUtil.getFields(modelClass)) {
                String name = field.getName();
                Object value = ReflectUtil.getFieldValue(modelObject, field);
                String valStr = Objects.nonNull(value) ? value.toString() : "";
                String note = (map != null && map.containsKey(name)) ? map.get(name) : "";
                String text = note + name + "=" + valStr + "\n";
                writer.append(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("写入文件失败");
        }
    }

    private static List<FieldInfo> readInputStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<FieldInfo> result = new ArrayList<>();

            String line;
            StringBuilder note = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    note.append(line).append("\n");
                }
                else {
                    int i = line.indexOf("=");
                    if (i != -1) {
                        String key = line.substring(0, i);
                        String val = line.substring(i + 1);
                        result.add(new FieldInfo(key, val, note.toString()));
                        note = new StringBuilder();
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("读取文件流失败");
        }
    }

    @Data
    @AllArgsConstructor
    private static class FieldInfo {
        private String name;
        private String value;
        private String note;
    }

}
