package com.amc.util.reflect.core;

import com.amc.util.common.model.GenericType;

import java.lang.reflect.*;
import java.util.*;

public class GenericUtil {

    public static final String PARAM_TYPE = "param_type";
    public static final String RETURN_TYPE = "return_type";

    /**
     * 获取父类的泛型信息
     */
    public static GenericType getSuperGeneric(Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        Type superType = clazz.getGenericSuperclass();
        return getGenericType(superClass, superType, null);
    }

    /**
     * 获取接口类们的泛型信息
     */
    public static List<GenericType> getInterfaceGenerics(Class<?> clazz) {
        List<GenericType> result = new ArrayList<>();
        Class<?>[] interfaces = clazz.getInterfaces();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            GenericType generics = getGenericType(interfaces[i], genericInterfaces[i], null);
            result.add(generics);
        }
        return result;
    }

    /**
     * 获取所有属性的泛型信息
     */
    public static Map<String, GenericType> getFieldGenerics(Class<?> clazz) {
        Map<String, GenericType> result = new LinkedHashMap<>();
        Map<String, GenericType> map = null;

        do {
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> fieldClass = field.getType();
                Type fieldType = field.getGenericType();
                GenericType genericType = getGenericType(fieldClass, fieldType, map);
                result.put(field.getName(), genericType);
            }

            Class<?> superClass = clazz.getSuperclass();
            Type superType = clazz.getGenericSuperclass();
            List<GenericType> genericTypes = getGenericType(superClass, superType, map).getGenericTypes();
            map = new HashMap<>();
            if (!genericTypes.isEmpty()) {
                TypeVariable<?>[] typeParameters = superClass.getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    map.put(typeParameters[i].getName(), genericTypes.get(i));
                }
            }
            clazz = superClass;
        } while (!Objects.equals(clazz, Object.class));

        return result;
    }

    /**
     * 获取所有方法的参数泛型信息
     */
    public static Map<String, List<GenericType>> getMethodParameterGenerics(Class<?> clazz) {
        Map<String, List<GenericType>> result = new LinkedHashMap<>();
        Map<String, Map<String, Object>> methodGenerics = getMethodGenerics(clazz);
        methodGenerics.forEach((k, v) -> {
            List<GenericType> paramTypes = (List<GenericType>) v.get(PARAM_TYPE);
            result.put(k, paramTypes);
        });
        return result;
    }

    /**
     * 获取所有方法的返回类型泛型信息
     */
    public static Map<String, GenericType> getMethodReturnGenerics(Class<?> clazz) {
        Map<String, GenericType> result = new LinkedHashMap<>();
        Map<String, Map<String, Object>> methodGenerics = getMethodGenerics(clazz);
        methodGenerics.forEach((k, v) -> {
            GenericType returnType = (GenericType) v.get(RETURN_TYPE);
            result.put(k, returnType);
        });
        return result;
    }

    /**
     * 获取所有方法的泛型信息
     */
    public static Map<String, Map<String, Object>> getMethodGenerics(Class<?> clazz) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        Map<String, GenericType> map = null;

        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isBridge()) {
                    continue;
                }

                List<GenericType> paramList = new ArrayList<>();
                Parameter[] parameters = method.getParameters();
                Type[] parameterTypes = method.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> parameterClass = parameters[i].getType();
                    GenericType parameterGeneric = getGenericType(parameterClass, parameterTypes[i], map);
                    paramList.add(parameterGeneric);
                }

                Class<?> returnClass = method.getReturnType();
                Type returnType = method.getGenericReturnType();
                GenericType returnGeneric = getGenericType(returnClass, returnType, map);

                String methodId = MethodUtil.getMethodSignature(method);
                Map<String, Object> node = new HashMap<>();
                node.put(PARAM_TYPE, paramList);
                node.put(RETURN_TYPE, returnGeneric);
                result.put(methodId, node);
            }

            Class<?> superClass = clazz.getSuperclass();
            Type superType = clazz.getGenericSuperclass();
            List<GenericType> genericTypes = getGenericType(superClass, superType, map).getGenericTypes();
            map = new HashMap<>();
            if (!genericTypes.isEmpty()) {
                TypeVariable<?>[] typeParameters = superClass.getTypeParameters();
                for (int i = 0; i < typeParameters.length; i++) {
                    map.put(typeParameters[i].getName(), genericTypes.get(i));
                }
            }
            clazz = superClass;
        } while (!Objects.equals(clazz, Object.class));

        return result;
    }

    private static GenericType getGenericType(Class<?> clazz, Type type, Map<String, GenericType> map) {
        List<GenericType> genericTypes = new ArrayList<>();

        GenericType result = new GenericType();
        result.setClazz(clazz);
        result.setGenericTypes(genericTypes);
        result.setSignature(type.getTypeName());

        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            for (Type genericType : paramType.getActualTypeArguments()) {
                GenericType gt = getGenericType(Object.class, genericType, map);
                while (true) {
                    if (genericType instanceof Class) {
                        gt.setClazz((Class<?>) genericType);
                    }
                    else if (genericType instanceof ParameterizedType) {
                        Type rawType = ((ParameterizedType) genericType).getRawType();
                        if (rawType instanceof Class) {
                            gt.setClazz((Class<?>) rawType);
                        }
                    }
                    else if (genericType instanceof WildcardType) {
                        WildcardType wildcardType = (WildcardType) genericType;
                        Type[] lowerBounds = wildcardType.getLowerBounds();
                        if (lowerBounds.length > 0) {
                            genericType = lowerBounds[0];
                        } else {
                            genericType = wildcardType.getUpperBounds()[0];
                        }
                        gt = getGenericType(Object.class, genericType, map);
                        continue;
                    }
                    break;
                }
                genericTypes.add(gt);
            }
        }
        else if (type instanceof TypeVariable) {
            if (Objects.nonNull(map)) {
                GenericType genericType = map.get(type.getTypeName());
                if (Objects.nonNull(genericType)) {
                    result = genericType;
                }
            }
        }
        else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();

            while (true) {
                if (componentType instanceof TypeVariable) {
                    if (Objects.nonNull(map)) {
                        String typeName = type.getTypeName();
                        String genericName = componentType.getTypeName();
                        int dimension = (typeName.length() - genericName.length()) / 2;

                        GenericType genericType = map.get(genericName);
                        if (Objects.nonNull(genericType)) {
                            Class<?> objectClass = genericType.getClazz();
                            Class<?> arrayClass = ClassUtil.getArrayClass(objectClass, dimension);
                            result.setClazz(arrayClass);
                        }
                    }
                }
                else if (componentType instanceof ParameterizedType) {
                    List<GenericType> newGenericTypes = getGenericType(null, componentType, map).getGenericTypes();
                    result.setGenericTypes(newGenericTypes);
                }
                else if (componentType instanceof GenericArrayType) {
                    componentType = ((GenericArrayType) componentType).getGenericComponentType();
                    continue;
                }
                break;
            }
        }

        return result;
    }

}
