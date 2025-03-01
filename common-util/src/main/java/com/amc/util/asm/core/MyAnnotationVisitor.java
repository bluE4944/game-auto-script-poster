package com.amc.util.asm.core;

import com.amc.util.common.model.AnnotationMetaData;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyAnnotationVisitor extends AnnotationVisitor {

    private final Map<String, Object> valueMap;

    public MyAnnotationVisitor(int api, AnnotationMetaData annotationMetaData) {
        super(api);
        this.valueMap = annotationMetaData.getValueMap();
    }

    @Override
    public void visit(String name, Object value) {
        value = parseValue(value);
        valueMap.put(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        String enumClassName = AsmApi.parseTypeText(descriptor);
        value = enumClassName + "." + value;
        valueMap.put(name, value);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        List<Object> array = new ArrayList<>();
        valueMap.put(name, array);

        return new AnnotationVisitor(api) {
            @Override
            public void visit(String name, Object value) {
                array.add(value);
            }
            @Override
            public void visitEnum(String name, String descriptor, String value) {
                String enumClassName = AsmApi.parseTypeText(descriptor);
                value = enumClassName + "." + value;
                array.add(value);
            }
            @Override
            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                String annotationClassName = AsmApi.parseTypeText(descriptor);
                AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationClassName);
                array.add(annotationMetaData);
                return new MyAnnotationVisitor(api, annotationMetaData);
            }
        };
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        String annotationClassName = AsmApi.parseTypeText(descriptor);
        AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationClassName);
        valueMap.put(name, annotationMetaData);
        return new MyAnnotationVisitor(api, annotationMetaData);
    }

    private Object parseValue(Object value) {
        // Class类型
        if (value instanceof Type) {
            Type type = (Type) value;
            return type.getClassName();
        }
        // 简单类型数组
        else if (value.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            if (value instanceof int[]) {
                for (int e : ((int[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof double[]) {
                for (double e : ((double[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof long[]) {
                for (long e : ((long[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof boolean[]) {
                for (boolean e : ((boolean[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof float[]) {
                for (float e : ((float[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof short[]) {
                for (short e : ((short[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof char[]) {
                for (char e : ((char[]) value)) {
                    list.add(e);
                }
            }
            else if (value instanceof byte[]) {
                for (byte e : ((byte[]) value)) {
                    list.add(e);
                }
            }
            return list;
        }
        // 基础类型
        else {
            return value;
        }
    }

}
