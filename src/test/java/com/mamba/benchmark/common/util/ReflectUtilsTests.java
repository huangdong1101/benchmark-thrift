package com.mamba.benchmark.common.util;

import com.alibaba.fastjson.JSON;
import com.mamba.benchmark.common.JarFileLoader;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

class ReflectUtilsTests {

    @Test
    void test_loadClass() throws IOException, ClassNotFoundException, NoSuchMethodException {
        String jarFile = this.getClass().getClassLoader().getResource("sample.jar").getFile();
        try (JarFileLoader classLoader = new JarFileLoader(new File(jarFile))) {
            Class<?> clientClass = classLoader.loadClass("SharedService$Client");
            Assertions.assertTrue(TServiceClient.class.isAssignableFrom(clientClass));

            Class<?> clientFactoryClass = ReflectUtils.findInnerClass(clientClass, "Factory");
            Assertions.assertTrue(TServiceClientFactory.class.isAssignableFrom(clientFactoryClass));

            Method method = ReflectUtils.findMethod(clientClass, "getStruct");
            Assertions.assertNotNull(method);

            String[] argsStr = {"1", "zhangsan-token", "{\"key\":1,\"value\":\"lisi\"}"};
            Object[] args = ReflectUtils.castArgs(method, argsStr);
            Assertions.assertEquals(args.length, 3);
            Assertions.assertEquals(args[0], Integer.valueOf(argsStr[0]));
            Assertions.assertEquals(args[1], argsStr[1]);
            Assertions.assertEquals(args[2], JSON.parseObject(argsStr[2], method.getGenericParameterTypes()[2]));
        }
    }
}
