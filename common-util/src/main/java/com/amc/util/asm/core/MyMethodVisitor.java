package com.amc.util.asm.core;

import com.amc.util.common.model.AnnotationMetaData;
import com.amc.util.common.model.FieldMetaData;
import com.amc.util.common.model.MethodMetaData;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class MyMethodVisitor extends MethodVisitor {

    private final MethodMetaData methodMetaData;
    private final List<FieldMetaData> parameterList;

    public MyMethodVisitor(int api, MethodMetaData methodMetaData) {
        super(api);
        this.methodMetaData = methodMetaData;
        this.parameterList = methodMetaData.getParameterList();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (!methodMetaData.isStatic() && index-- == 0) {
            return;
        }
        if (index < parameterList.size()) {
            FieldMetaData fieldMetaData = parameterList.get(index);
            fieldMetaData.setName(name);
        }
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        FieldMetaData fieldMetaData = parameterList.get(parameter);
        String annotationName = AsmApi.parseTypeText(descriptor);
        AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationName);
        fieldMetaData.getAnnotationList().add(annotationMetaData);
        return new MyAnnotationVisitor(api, annotationMetaData);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        List<AnnotationMetaData> annotationList = methodMetaData.getAnnotationList();
        String annotationName = AsmApi.parseTypeText(descriptor);
        AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationName);
        annotationList.add(annotationMetaData);
        return new MyAnnotationVisitor(api, annotationMetaData);
    }

}
