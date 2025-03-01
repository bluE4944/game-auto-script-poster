package com.amc.util.asm.core;

import com.amc.util.common.model.ClassMetaData;
import com.amc.util.common.model.FieldMetaData;
import com.amc.util.common.model.MethodMetaData;
import com.amc.util.common.model.SignatureMetaData;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MySignatureVisitor {

    private final static Integer API = Opcodes.ASM7;

    protected static void readClass(String signature, ClassMetaData classMetaData, String superName) {
        SignatureMetaData signatureMetaData = classMetaData.getSignature();
        signatureMetaData.setSuperName(superName);

        if (Objects.isNull(signature)) {
            return;
        }

        String className = classMetaData.getClassName();
        List<String> generics = signatureMetaData.getGenerics();
        Map<String, String> genericClass = signatureMetaData.getGenericClass();
        List<String> superGenericClass = signatureMetaData.getSuperGenericClass();
        SignatureReader sr = new SignatureReader(signature);
        sr.accept(new SignatureVisitor(API) {
            @Override
            public void visitFormalTypeParameter(String name) {
                generics.add(name);
            }
            @Override
            public SignatureVisitor visitClassBound() {
                return new SignatureVisitor(API) {
                    @Override
                    public void visitClassType(String name) {
                        String genericName = generics.get(generics.size() - 1);
                        String genericType = AsmApi.replaceText(name);
                        genericClass.put(genericName, genericType);
                    }
                };
            }
            @Override
            public SignatureVisitor visitSuperclass() {
                return new SignatureVisitor(API) {
                    final List<List<String>> stack = new ArrayList<>();
                    @Override
                    public void visitClassType(String name) {
                        name = AsmApi.replaceText(name);
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                    }
                    @Override
                    public void visitTypeVariable(String name) {
                        name = AsmApi.replaceText(name);
                        List<String> list = new ArrayList<>();
                        stack.add(list);

                        if (generics.contains(name)) {
                            name = AsmApi.getGenericType(className, name);
                            list.add(name);
                            visitEnd();
                            return;
                        }

                        list.add(name);
                    }
                    @Override
                    public void visitEnd() {
                        int size = stack.size();
                        if (Objects.equals(size, 1)) {
                            return;
                        }

                        List<String> list0 = stack.get(size - 1);
                        List<String> list1 = stack.get(size - 2);
                        stack.remove(list0);
                        String type = resolveType(list0);
                        list1.add(type);

                        if (Objects.equals(size, 2)) {
                            superGenericClass.add(type);
                        }
                    }
                };
            }
        });
    }

    protected static void readField(String signature, FieldMetaData fieldMetaData, ClassMetaData classMetaData) {
        if (Objects.isNull(signature)) {
            return;
        }

        String className = classMetaData.getClassName();
        SignatureReader sr = new SignatureReader(signature);
        sr.accept(new SignatureVisitor(API) {
            final List<List<String>> stack = new ArrayList<>();
            @Override
            public void visitClassType(String name) {
                name = AsmApi.replaceText(name);
                List<String> list = new ArrayList<>();
                list.add(name);
                stack.add(list);
            }
            @Override
            public void visitTypeVariable(String name) {
                name = AsmApi.getGenericType(className, name);
                List<String> list = new ArrayList<>();
                list.add(name);
                stack.add(list);
                visitEnd();
            }
            @Override
            public void visitEnd() {
                int size = stack.size();
                List<String> list0 = stack.get(size - 1);

                if (Objects.equals(size, 1)) {
                    String newType = resolveType(list0);
                    fieldMetaData.setType(newType);
                    return;
                }

                List<String> list1 = stack.get(size - 2);
                stack.remove(list0);
                String type = resolveType(list0);
                list1.add(type);

                if (Objects.equals(size, 2)) {
                    String newType = resolveType(list1);
                    fieldMetaData.setType(newType);
                }
            }
            @Override
            public SignatureVisitor visitArrayType() {
                return new SignatureVisitor(API) {
                    @Override
                    public void visitClassType(String name) {
                        name = AsmApi.replaceText(name);
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                    }
                    @Override
                    public void visitTypeVariable(String name) {
                        name = AsmApi.getGenericType(className, name);
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                        visitEnd();
                    }
                    @Override
                    public void visitEnd() {
                        int size = stack.size();
                        List<String> list0 = stack.get(size - 1);

                        if (Objects.equals(size, 1)) {
                            String oldType = fieldMetaData.getType();
                            String newType = resolveType(list0);
                            String suffix = oldType.substring(oldType.indexOf("["));
                            fieldMetaData.setType(newType + suffix);
                            return;
                        }

                        List<String> list1 = stack.get(size - 2);
                        stack.remove(list0);
                        String type = resolveType(list0);
                        list1.add(type);

                        if (Objects.equals(size, 2)) {
                            String oldType = fieldMetaData.getType();
                            String newType = resolveType(list1);
                            String suffix = oldType.substring(oldType.indexOf("["));
                            fieldMetaData.setType(newType + suffix);
                        }
                    }
                };
            }
        });
    }

    protected static void readMethod(String signature, MethodMetaData methodMetaData, ClassMetaData classMetaData) {
        if (Objects.isNull(signature)) {
            return;
        }

        String className = classMetaData.getClassName();
        String objectName = Object.class.getName();
        List<String> generics = classMetaData.getSignature().getGenerics();
        List<FieldMetaData> parameterList = methodMetaData.getParameterList();

        SignatureReader sr = new SignatureReader(signature);
        sr.accept(new SignatureVisitor(API) {
            final AtomicInteger paramIndex = new AtomicInteger(0);
            final AtomicInteger throwsIndex = new AtomicInteger(0);
            @Override
            public SignatureVisitor visitReturnType() {
                return new SignatureVisitor(API) {
                    final List<List<String>> stack = new ArrayList<>();
                    @Override
                    public void visitClassType(String name) {
                        name = AsmApi.replaceText(name);
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                    }
                    @Override
                    public void visitTypeVariable(String name) {
                        name = generics.contains(name) ? AsmApi.getGenericType(className, name) : objectName;
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                        visitEnd();
                    }
                    @Override
                    public void visitEnd() {
                        int size = stack.size();
                        List<String> list0 = stack.get(size - 1);

                        if (Objects.equals(size, 1)) {
                            String oldType = methodMetaData.getReturnType();
                            int i = oldType.indexOf("[");
                            String suffix = Objects.equals(i, -1) ? "" : oldType.substring(i);
                            String newType = resolveType(list0);
                            methodMetaData.setReturnType(newType + suffix);
                            return;
                        }

                        List<String> list1 = stack.get(size - 2);
                        stack.remove(list0);
                        String type = resolveType(list0);
                        list1.add(type);

                        if (Objects.equals(size, 2)) {
                            String oldType = methodMetaData.getReturnType();
                            int i = oldType.indexOf("[");
                            String suffix = Objects.equals(i, -1) ? "" : oldType.substring(i);
                            String newType = resolveType(list1);
                            methodMetaData.setReturnType(newType + suffix);
                        }
                    }
                };
            }
            @Override
            public SignatureVisitor visitParameterType() {
                return new SignatureVisitor(API) {
                    final List<List<String>> stack = new ArrayList<>();
                    final FieldMetaData parameterMetaData = parameterList.get(paramIndex.getAndIncrement());
                    @Override
                    public void visitClassType(String name) {
                        name = AsmApi.replaceText(name);
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                    }
                    @Override
                    public void visitTypeVariable(String name) {
                        name = generics.contains(name) ? AsmApi.getGenericType(className, name) : objectName;
                        List<String> list = new ArrayList<>();
                        list.add(name);
                        stack.add(list);
                        visitEnd();
                    }
                    @Override
                    public void visitEnd() {
                        int size = stack.size();
                        List<String> list0 = stack.get(size - 1);

                        if (Objects.equals(size, 1)) {
                            String newType = resolveType(list0);
                            parameterMetaData.setType(newType);
                            return;
                        }

                        List<String> list1 = stack.get(size - 2);
                        stack.remove(list0);
                        String type = resolveType(list0);
                        list1.add(type);

                        if (Objects.equals(size, 2)) {
                            String newType = resolveType(list1);
                            parameterMetaData.setType(newType);
                        }
                    }
                    @Override
                    public SignatureVisitor visitArrayType() {
                        return new SignatureVisitor(API) {
                            @Override
                            public void visitClassType(String name) {
                                name = AsmApi.replaceText(name);
                                List<String> list = new ArrayList<>();
                                list.add(name);
                                stack.add(list);
                            }
                            @Override
                            public void visitTypeVariable(String name) {
                                name = generics.contains(name) ? AsmApi.getGenericType(className, name) : objectName;
                                List<String> list = new ArrayList<>();
                                list.add(name);
                                stack.add(list);
                                visitEnd();
                            }
                            @Override
                            public void visitEnd() {
                                int size = stack.size();
                                List<String> list0 = stack.get(size - 1);

                                if (Objects.equals(size, 1)) {
                                    String oldType = parameterMetaData.getType();
                                    String newType = resolveType(list0);
                                    String suffix = oldType.substring(oldType.indexOf("["));
                                    parameterMetaData.setType(newType + suffix);
                                    return;
                                }

                                List<String> list1 = stack.get(size - 2);
                                stack.remove(list0);
                                String type = resolveType(list0);
                                list1.add(type);

                                if (Objects.equals(size, 2)) {
                                    String oldType = parameterMetaData.getType();
                                    String newType = resolveType(list1);
                                    String suffix = oldType.substring(oldType.indexOf("["));
                                    parameterMetaData.setType(newType + suffix);
                                }
                            }
                        };
                    }
                };
            }
            @Override
            public SignatureVisitor visitExceptionType() {
                return new SignatureVisitor(API) {
                    final Integer index = throwsIndex.getAndIncrement();
                    final List<String> throwNames = methodMetaData.getThrowExceptionNames();

                    @Override
                    public void visitTypeVariable(String name) {
                        name = generics.contains(name) ? AsmApi.getGenericType(className, name) : Exception.class.getName();
                        throwNames.set(index, name);
                    }
                };
            }
        });
    }

    private static String resolveType(List<String> typeList) {
        int size = typeList.size();
        String type = typeList.get(0);
        if (Objects.equals(size, 1)) {
            return type;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(type).append("<");
        for (int i = 1; i < typeList.size(); i++) {
            buf.append(typeList.get(i)).append(", ");
        }
        buf.delete(buf.length() - 2, buf.length());
        buf.append(">");
        return buf.toString();
    }

}
