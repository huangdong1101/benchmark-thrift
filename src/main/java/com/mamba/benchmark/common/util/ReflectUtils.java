package com.mamba.benchmark.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class ReflectUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Class<?> findInnerClass(Class<?> clazz, String name) throws ClassNotFoundException {
        Class<?>[] innerClasses = clazz.getClasses();
        for (Class innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals(name)) {
                return innerClass;
            }
        }
        throw new ClassNotFoundException("Invalid inner class: " + clazz.getName() + '$' + name);
    }

    public static Method findMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Invalid method: " + clazz.getName() + '.' + name);
    }

    public static Object[] castArgs(Method method, String... args) {
        int parameterCount = method.getParameterCount();
        if (method.getParameterCount() == 0) {
            if (args == null || args.length == 0) {
                return new Object[0];
            }
            throw new IllegalArgumentException("Invalid args. expect parameter count: 0, actual args size: " + args.length);
        }
        if (args == null) {
            throw new IllegalArgumentException("Null args. expect parameter count: " + parameterCount);
        }
        if (args.length != parameterCount) {
            throw new IllegalArgumentException("Invalid args. expect parameter count: " + parameterCount + ", actual args size: " + args.length);
        }
        Object[] arguments = new Object[parameterCount];
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterCount; i++) {
            arguments[i] = cast(args[i], parameterTypes[i]);
        }
        return arguments;
    }

    public static Object cast(String s, Type type) {
        if (s == null) {
            return null;
        }
        if (type == String.class) {
            return s;
        }
        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(s);
        }
        if (type == short.class || type == Short.class) {
            return Short.valueOf(s);
        }
        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(s);
        }
        if (type == long.class || type == Long.class) {
            return Long.valueOf(s);
        }
        if (type == float.class || type == Float.class) {
            return Float.valueOf(s);
        }
        if (type == double.class || type == Double.class) {
            return Double.valueOf(s);
        }
        if (type == char.class || type == Character.class) {
            if (s.isEmpty()) {
                return null;
            }
            if (s.length() == 1) {
                return s.charAt(0);
            }
            String tmp = s.trim();
            if (tmp.isEmpty()) {
                return ' ';
            }
            if (tmp.length() == 1) {
                return tmp.charAt(0);
            }
            throw new IllegalArgumentException("Invalid char: " + s);
        }
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(s);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(s);
        }
        if (type == BigInteger.class) {
            return new BigInteger(s);
        }
        try {
            return MAPPER.readValue(s, MAPPER.constructType(type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error data('" + type.getTypeName() + "') " + s + " : " + e.getMessage(), e);
        }
    }
}
