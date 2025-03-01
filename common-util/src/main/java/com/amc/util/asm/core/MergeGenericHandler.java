package com.amc.util.asm.core;

import com.amc.util.common.model.ClassMetaData;
import com.amc.util.common.model.FieldMetaData;
import com.amc.util.common.model.SignatureMetaData;
import com.amc.util.json.JsonUtil;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class MergeGenericHandler {

    public static ClassMetaData doMerge(ClassMetaData classMetaData, String... genericClassNames) {
        String json = JsonUtil.toJSONString(classMetaData);
        ClassMetaData result = JsonUtil.parseObject(json, ClassMetaData.class);

        if (genericClassNames.length > 0) {
            SignatureMetaData signature = result.getSignature();
            List<String> generics = signature.getGenerics();
            Map<String, String> genericClass = signature.getGenericClass();
            for (int i = 0; i < genericClassNames.length && i < generics.size(); i++) {
                genericClass.put(generics.get(i), genericClassNames[i]);
            }
            classMetaData = result;
        }

        Map<String, String> replaceMap = new LinkedHashMap<>();
        Queue<String> superGenericQueue = new LinkedBlockingQueue<>();
        while (true) {
            String className = classMetaData.getClassName();
            SignatureMetaData signature = classMetaData.getSignature();

            signature.getGenericClass().forEach((genericName, genericClazz) -> {
                String generic = AsmApi.getGenericType(className, genericName);

                String genericClassName = superGenericQueue.poll();
                if (Objects.isNull(genericClassName)) {
                    genericClassName = genericClazz;
                }

                replaceMap.put(generic, genericClassName);
                replaceMap.forEach((k, v) -> {
                    String oldType = replaceMap.get(generic);
                    if (oldType.contains(k)) {
                        String newType = oldType.replaceAll(k, v);
                        replaceMap.put(generic, newType);
                    }
                });
            });

            String superName = signature.getSuperName();
            if (Objects.equals(superName, Object.class.getName())) {
                break;
            }

            signature.getSuperGenericClass().forEach(data -> {
                if (replaceMap.containsKey(data)) {
                    data = replaceMap.get(data);
                }
                superGenericQueue.offer(data);
            });

            classMetaData = ClassMetaDataReader.readClass(superName);
        }

        result.getFieldList().forEach(fieldMetaData -> {
            replaceMap.forEach((k, v) -> {
                String oldType = fieldMetaData.getType();
                if (oldType.contains(k)) {
                    String newType = oldType.replaceAll(k, v);
                    fieldMetaData.setType(newType);
                }
            });
        });

        result.getMethodList().forEach(methodMetaData -> {
            replaceMap.forEach((k, v) -> {
                String oldType = methodMetaData.getReturnType();
                if (oldType.contains(k)) {
                    String newType = oldType.replaceAll(k, v);
                    methodMetaData.setReturnType(newType);
                }
            });

            List<FieldMetaData> parameterList = methodMetaData.getParameterList();
            parameterList.forEach(paramMetaData -> {
                replaceMap.forEach((k, v) -> {
                    String oldType = paramMetaData.getType();
                    if (oldType.contains(k)) {
                        String newType = oldType.replaceAll(k, v);
                        paramMetaData.setType(newType);
                    }
                });
            });

            List<String> throwExceptionNames = methodMetaData.getThrowExceptionNames();
            for (int i = 0; i < throwExceptionNames.size(); i++) {
                String oldType = throwExceptionNames.get(i);
                if (replaceMap.containsKey(oldType)) {
                    String newType = replaceMap.get(oldType);
                    throwExceptionNames.set(i, newType);
                }
            }
        });

        return result;
    }

}
