package com.amc.util.asm.core;

import com.amc.util.common.model.AnnotationMetaData;
import com.amc.util.common.model.FieldMetaData;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.List;

public class MyFieldVisitor extends FieldVisitor {

    private final List<AnnotationMetaData> annotationList;

    public MyFieldVisitor(int api, FieldMetaData fieldMetaData) {
        super(api);
        this.annotationList = fieldMetaData.getAnnotationList();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String annotationName = AsmApi.parseTypeText(descriptor);
        AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationName);
        annotationList.add(annotationMetaData);
        return new MyAnnotationVisitor(api, annotationMetaData);
    }

}
