package com.mamba.benchmark.thrift.define;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.mamba.benchmark.common.util.BenchmarkUtils;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

public class Request<T extends TServiceClient> {

//    private final Class<T> clientClass;

    private final TServiceClientFactory<T> clientFactory;

    private final Method method;

    private final Object[] arguments;

    public Request(Class<T> clientClass, String method, Object... arguments) {
//        this.clientClass = clientClass;
        this.method = getMethod(clientClass, method);
        this.arguments = arguments;
        this.clientFactory = newServiceClientFactory(clientClass);
    }

    public TServiceClientFactory<T> getClientFactory() {
        return clientFactory;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public static Request parse(URLClassLoader classLoader, JSONObject interfaceDescription, String text) {
        JSONObject json = JSONObject.parseObject(text);
        String service = json.getString("service");
        if (Strings.isNullOrEmpty(service)) {
            throw new RuntimeException("Invalid request: " + text);
        }
        String method = json.getString("method");
        if (Strings.isNullOrEmpty(method)) {
            throw new RuntimeException("Invalid request: " + text);
        }
        JSONArray argument = json.getJSONArray("arguments");
        return parse(classLoader, interfaceDescription, service, method, argument.toArray());
    }

    private static Request parse(URLClassLoader classLoader, JSONObject interfaceDescription, String serviceName, String methodName, Object... arguments) {
        String className = getClassName(interfaceDescription, serviceName, methodName);
        Class<?> serviceClass = loadClass(classLoader, className);
        Class<? extends TServiceClient> clientClass = findInnerClass(serviceClass, "Client", TServiceClient.class);
        Method method = getMethod(clientClass, methodName);
        return new Request(clientClass, methodName, parseArguments(method.getParameterTypes(), arguments));
    }

    private static Object[] parseArguments(Class<?>[] parameterTypes, Object[] originalArguments) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            if (originalArguments != null && originalArguments.length > 0) {
                throw new IllegalArgumentException("Invalid arguments count in request config. expect: 0, actual: " + originalArguments.length);
            }
            return new Object[0];
        }
        if (originalArguments == null) {
            throw new IllegalArgumentException("Null arguments in request config. expect: " + parameterTypes.length);
        }
        if (originalArguments.length != parameterTypes.length) {
            throw new IllegalArgumentException("Invalid arguments count in request config. expect: " + parameterTypes.length + ", actual: " + originalArguments.length);
        }
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = BenchmarkUtils.cast(originalArguments[i], parameterTypes[i]);
        }
        return arguments;
    }

    private static String getClassName(JSONObject interfaceDescription, String serviceName, String methodName) {
        JSONArray services = interfaceDescription.getJSONArray("services");
        for (int i = 0; i < services.size(); i++) {
            JSONObject service = services.getJSONObject(i);
            if (serviceName.equals(service.getString("service")) && containsMethod(service.getJSONArray("methods"), methodName)) {
                String namespace = service.getString("namespace");
                if (Strings.isNullOrEmpty(namespace)) {
                    return serviceName;
                } else {
                    return namespace + '.' + serviceName;
                }
            }
        }
        return null;
    }

    private static boolean containsMethod(JSONArray methods, String methodName) {
        for (int i = 0; i < methods.size(); i++) {
            if (methodName.equals(methods.getJSONObject(i).getString("method"))) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> loadClass(URLClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassNotFound: " + name, e);
        }
    }

    private static <T> Class<? extends T> findInnerClass(Class<?> clazz, String name, Class<T> superClass) {
        for (Class<?> innerClass : clazz.getClasses()) {
            if (!innerClass.getSimpleName().equals(name)) {
                continue;
            }
            if (!superClass.isAssignableFrom(innerClass)) {
                throw new IllegalStateException("Invalid innerClass: " + innerClass.getName());
            }
            return (Class<? extends T>) innerClass;
        }
        throw new IllegalStateException("InnerClassNotFound: " + clazz.getName() + '$' + name);
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new IllegalStateException("Invalid method: " + clazz.getName() + "#" + methodName);
    }

    private static <T extends TServiceClient> TServiceClientFactory<T> newServiceClientFactory(Class<T> clientClass) {
        Class<TServiceClientFactory<T>> clientFactoryClass = (Class<TServiceClientFactory<T>>) findInnerClass(clientClass, "Factory", TServiceClientFactory.class);
        try {
            return clientFactoryClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Error instantiation for class '" + clientFactoryClass.getName() + "' : " + e.getMessage(), e);
        }
    }
}
