package com.amc.util.asm.core;

import com.amc.util.common.model.*;
import org.objectweb.asm.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字节码文件读取器
 */
public class ClassMetaDataReader extends ClassVisitor {

    private ClassMetaData classMetaData;

    private final static Integer API = Opcodes.ASM7;
    private final static Map<String, ClassMetaData> cacheMap = new HashMap<>();

    private ClassMetaDataReader() {
        super(API);
    }

    public static ClassMetaData readClass(String className) {
        if (cacheMap.containsKey(className)) {
            return cacheMap.get(className);
        }
        try {
            ClassMetaDataReader metaDataReader = new ClassMetaDataReader();
            metaDataReader.doRead(className);
            metaDataReader.mergeClassMetaData();
            return metaDataReader.classMetaData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("解析字节码失败: " + className);
        }
    }

    private void doRead(String className) throws Exception {
        ClassReader classReader = new ClassReader(className);
        classMetaData = new ClassMetaData();
        cacheMap.put(className, classMetaData);
        classReader.accept(this, 0);
    }

    /**
     * 解析class的基础信息
     */
    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        className = AsmApi.replaceText(className);
        superName = AsmApi.replaceText(superName);
        List<String> interfaceNames = Arrays.stream(interfaces).map(AsmApi::replaceText).collect(Collectors.toList());

        classMetaData.setAccess(access);
        classMetaData.setClassName(className);
        classMetaData.getSuperNames().add(superName);
        classMetaData.getInterfaces().addAll(interfaceNames);
        classMetaData.setSignature(new SignatureMetaData());
        MySignatureVisitor.readClass(signature, classMetaData, superName);
    }

    /**
     * 解析class的内部类信息
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        outerName = AsmApi.replaceText(outerName);
        if (Objects.equals(outerName, classMetaData.getClassName())) {
            name = AsmApi.replaceText(name);
            classMetaData.getInnerClassNames().add(name);
        }
    }

    /**
     * 解析class的注解信息
     */
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String annotationClassName = AsmApi.parseTypeText(descriptor);
        AnnotationMetaData annotationMetaData = new AnnotationMetaData(annotationClassName);
        classMetaData.getAnnotationList().add(annotationMetaData);
        return new MyAnnotationVisitor(API, annotationMetaData);
    }

    /**
     * 解析class的属性信息
     */
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldMetaData fieldMetaData = new FieldMetaData();
        fieldMetaData.setAccess(access);
        fieldMetaData.setType(AsmApi.parseTypeText(descriptor));
        fieldMetaData.setName(name);
        classMetaData.getFieldList().add(fieldMetaData);
        MySignatureVisitor.readField(signature, fieldMetaData, classMetaData);
        return new MyFieldVisitor(API, fieldMetaData);
    }

    /**
     * 解析class的方法信息
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (BaseMetaData.isBridge(access)) return null;
        MethodMetaData methodMetaData = new MethodMetaData();
        initMethodMetaData(methodMetaData, access, name, descriptor, exceptions);
        classMetaData.getMethodList().add(methodMetaData);
        MySignatureVisitor.readMethod(signature, methodMetaData, classMetaData);
        return new MyMethodVisitor(API, methodMetaData);
    }

    private void initMethodMetaData(MethodMetaData methodMetaData, int access, String name, String descriptor, String[] exceptions) {
        Type methodType = Type.getMethodType(descriptor);
        Type returnType = methodType.getReturnType();
        Type[] argTypes = methodType.getArgumentTypes();

        List<FieldMetaData> parameterList = methodMetaData.getParameterList();
        for (int i = 0; i < argTypes.length; i++) {
            Type argType = argTypes[i];
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.setAccess(access);
            fieldMetaData.setType(argType.getClassName());
            fieldMetaData.setName("arg" + i);
            parameterList.add(fieldMetaData);
        }

        if (Objects.nonNull(exceptions)) {
            List<String> throwExceptionNames = Stream.of(exceptions).map(AsmApi::replaceText).collect(Collectors.toList());
            methodMetaData.getThrowExceptionNames().addAll(throwExceptionNames);
        }

        methodMetaData.setAccess(access);
        methodMetaData.setReturnType(returnType.getClassName());
        methodMetaData.setName(name);
    }

    private void mergeClassMetaData() {
        mergeSuper();
        mergeInterface();
        mergeAnnotation();
    }

    private void mergeSuper() {
        String superName = classMetaData.getSignature().getSuperName();
        if (Objects.equals(superName, Object.class.getName())) {
            return;
        }

        ClassMetaData superClassMetaData = readClass(superName);
        classMetaData.getSuperNames().addAll(superClassMetaData.getSuperNames());

        Set<String> innerClassNames = classMetaData.getInnerClassNames();
        superClassMetaData.getInnerClassNames().forEach(innerClassName -> {
            ClassMetaData innerClassMetaData = readClass(innerClassName);
            if (innerClassMetaData.isPublic()) {
                innerClassNames.add(innerClassName);
            }
        });

        List<FieldMetaData> fieldList = classMetaData.getFieldList();
        List<String> fieldNames = fieldList.stream().map(FieldMetaData::getName).collect(Collectors.toList());
        List<FieldMetaData> superFieldList = superClassMetaData.getFieldList().stream().filter(superField -> superField.isPublic() && !fieldNames.contains(superField.getName())).collect(Collectors.toList());
        fieldList.addAll(superFieldList);

        List<MethodMetaData> methodList = classMetaData.getMethodList();
        List<String> methodSignatures = methodList.stream().map(MethodMetaData::getSignature).collect(Collectors.toList());
        List<MethodMetaData> superMethodList = superClassMetaData.getMethodList().stream().filter(mmd -> mmd.isPublic() && !methodSignatures.contains(mmd.getSignature())).collect(Collectors.toList());
        methodList.addAll(superMethodList);
    }

    private void mergeInterface() {
        Set<String> interfaces = classMetaData.getInterfaces();
        Set<String> newInterfaces = new LinkedHashSet<>();
        interfaces.forEach(interfaceName -> {
            ClassMetaData interfaceMetaData = readClass(interfaceName);
            newInterfaces.addAll(interfaceMetaData.getInterfaces());
        });
        interfaces.addAll(newInterfaces);
    }

    private void mergeAnnotation() {
        List<AnnotationMetaData> annotationList = classMetaData.getAnnotationList();
        List<String> annotationNames = annotationList.stream().map(AnnotationMetaData::getClassName).collect(Collectors.toList());

        for (int i = 0; i < annotationNames.size(); i++) {
            String annotationName = annotationNames.get(i);
            ClassMetaData annotationMetaData = readClass(annotationName);

            List<AnnotationMetaData> newAnnotationList = annotationMetaData.getAnnotationList().stream().filter(md -> !annotationNames.contains(md.getClassName())).collect(Collectors.toList());
            List<String> newAnnotationNames = newAnnotationList.stream().map(AnnotationMetaData::getClassName).collect(Collectors.toList());
            annotationNames.addAll(newAnnotationNames);
            annotationList.addAll(newAnnotationList);
        }
    }

}
