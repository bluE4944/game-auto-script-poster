package com.amc.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * json工具类
 */
public class JsonUtil {

    private static final ObjectMapper om = new ObjectMapper();

    @SneakyThrows
    public static String toJSONString(Object data) {
        return om.writeValueAsString(data);
    }

    @SneakyThrows
    public static JsonNode parseObject(String json) {
        return om.readTree(json);
    }

    @SneakyThrows
    public static <T> T parseObject(String json, Class<T> clazz) {
        return om.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        JavaType javaType = getJavaType(List.class, clazz);
        return om.readValue(json, javaType);
    }

    @SneakyThrows
    public static <T> Set<T> parseSet(String json, Class<T> clazz) {
        JavaType javaType = getJavaType(Set.class, clazz);
        return om.readValue(json, javaType);
    }

    @SneakyThrows
    public static <T> Map<String, T> parseMap(String json, Class<T> clazz) {
        JavaType javaType = getJavaType(Map.class, String.class, clazz);
        return om.readValue(json, javaType);
    }


    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    static {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        om.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        om.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        om.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
        om.registerModule(javaTimeModule);
        om.setTimeZone(TimeZone.getDefault());
    }

    private static JavaType getJavaType(Class<?> parametrized, Class<?>... parameterClasses) {
        return om.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    public static JsonNode parseObject(String json, String expression) {
        JsonNode result = parseObject(json);
        Pattern pattern = Pattern.compile("^([^\\[]*)\\[(\\d*)\\]$");
        for (String field : expression.split("\\.")) {
            Matcher matcher = pattern.matcher(field);
            if (matcher.find()) {
                field = matcher.group(1);
                if (result.has(field)) {
                    result = result.get(field);
                }
                String indexStr = matcher.group(2);
                if (indexStr.length() > 0) {
                    int index = Integer.parseInt(indexStr);
                    result = result.get(index);
                }
            } else {
                if (result.has(field)) {
                    result = result.get(field);
                }
            }
        }
        return result;
    }

}