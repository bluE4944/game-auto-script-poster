package com.amc.util.reflect.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeParseUtil {

    /**
     * 类型转化
     */
    public static Object parse(Object val, Class<?> clazz) {
        Class<?> valClass = val.getClass();
        if (clazz.isAssignableFrom(valClass)) {
            return val;
        }
        Map<Class<?>, Converter> classConverterMap = converterMap.get(clazz);
        if (Objects.nonNull(classConverterMap)) {
            Converter converter = classConverterMap.get(valClass);
            if (Objects.nonNull(converter)) {
                return converter.convert(val);
            }
        }
        throw new RuntimeException("[" + val + "]转化[" + clazz + "]类型失败");
    }

    /**
     * 添加转换器
     */
    public static <K, V> void addConverter(Class<K> kClazz, Class<V> vClazz, Converter<K, V> converter) {
        Map<Class<?>, Converter> classConverterMap = converterMap.get(vClazz);
        if (Objects.isNull(classConverterMap)) {
            classConverterMap = new HashMap<>();
            converterMap.put(vClazz, classConverterMap);
        }
        classConverterMap.put(kClazz, converter);
    }

    private static final Map<Class<?>, Map<Class<?>, Converter>> converterMap = new HashMap<>();

    public interface Converter<K, V> {
        V convert(K source);
    }

    static {
        addConverter(String.class, Boolean.class, Boolean::parseBoolean);
        addConverter(String.class, Integer.class, Integer::parseInt);
        addConverter(String.class, Long.class, Long::parseLong);
        addConverter(String.class, Float.class, Float::parseFloat);
        addConverter(String.class, Double.class, Double::parseDouble);

        addConverter(Boolean.class, String.class, String::valueOf);
        addConverter(Boolean.class, Integer.class, val -> val ? 1 : 0);
        addConverter(Boolean.class, Long.class, val -> val ? 1L : 0);
        addConverter(Boolean.class, Float.class, val -> val ? 1F : 0);
        addConverter(Boolean.class, Double.class, val -> val ? 1D : 0);

        addConverter(Integer.class, String.class, String::valueOf);
        addConverter(Integer.class, Boolean.class, val -> val != 0);
        addConverter(Integer.class, Long.class, Integer::longValue);
        addConverter(Integer.class, Float.class, Integer::floatValue);
        addConverter(Integer.class, Double.class, Integer::doubleValue);

        addConverter(Long.class, String.class, String::valueOf);
        addConverter(Long.class, Boolean.class, val -> val != 0);
        addConverter(Long.class, Integer.class, Long::intValue);
        addConverter(Long.class, Float.class, Long::floatValue);
        addConverter(Long.class, Double.class, Long::doubleValue);

        addConverter(Float.class, String.class, String::valueOf);
        addConverter(Float.class, Boolean.class, val -> val != 0);
        addConverter(Float.class, Integer.class, Float::intValue);
        addConverter(Float.class, Long.class, Float::longValue);
        addConverter(Float.class, Double.class, Float::doubleValue);

        addConverter(Double.class, String.class, String::valueOf);
        addConverter(Double.class, Boolean.class, val -> val != 0);
        addConverter(Double.class, Integer.class, Double::intValue);
        addConverter(Double.class, Long.class, Double::longValue);
        addConverter(Double.class, Float.class, Double::floatValue);
    }

}
