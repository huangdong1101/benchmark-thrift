package com.mamba.benchmark.thrift.reflect;

import com.mamba.benchmark.thrift.client.TClientFactory;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Invoker<T extends TServiceClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Invoker.class);

    private TClientFactory<T> factory;

    public Invoker(TClientFactory<T> factory) {
        this.factory = factory;
    }

    public void invoke(Method method, Object... args) {
        T client;
        try {
            client = this.factory.getClient();
        } catch (Exception e) {
            LOGGER.info("{}#{} Error1: {} {}", getServiceName((Class<T>) method.getDeclaringClass()), method.getName(), e.getClass().getSimpleName(), e.getMessage());
            return;
        }
        try {
            this.invoke(client, method, args);
        } finally {
            this.factory.close(client);
        }
    }


    private void invoke(T client, Method method, Object... args) {
        long beginTime = System.currentTimeMillis();
        try {
            LOGGER.info("{}#{} Send", getServiceName(client.getClass()), method.getName());
            Object obj = method.invoke(client, args);
            long endTime = System.currentTimeMillis();
            LOGGER.info("{}#{} Response {} ms, success", getServiceName(client.getClass()), method.getName(), (endTime - beginTime));
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof TException) {
                if (t instanceof TBase) {
                    long endTime = System.currentTimeMillis();
                    LOGGER.info("{}#{} Response {} ms, error : {}", getServiceName(client.getClass()), method.getName(), (endTime - beginTime), t.toString());
                } else if (t instanceof TTransportException) {
                    LOGGER.info("{}#{} Error0: TTransportException {} {}", getServiceName(client.getClass()), method.getName(), ((TTransportException) t).getType(), t.getMessage());
                } else if (t instanceof TProtocolException) {
                    LOGGER.info("{}#{} Error0: TProtocolException {} {}", getServiceName(client.getClass()), method.getName(), ((TProtocolException) t).getType(), t.getMessage());
                } else if (t instanceof TApplicationException) {
                    LOGGER.info("{}#{} Error0: TApplicationException {} {}", getServiceName(client.getClass()), method.getName(), ((TApplicationException) t).getType(), t.getMessage());
                } else {
                    LOGGER.info("{}#{} Error0: {} {}", getServiceName(client.getClass()), method.getName(), t.getClass().getSimpleName(), t.getMessage());
                }
            } else {
                LOGGER.info("{}#{} Error2: {} {}", getServiceName(client.getClass()), method.getName(), t.getClass().getSimpleName(), t.getMessage());
            }
        } catch (IllegalAccessException e) {
            LOGGER.info("{}#{} Error2: {} {}", getServiceName(client.getClass()), method.getName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static <T extends TServiceClient> String getServiceName(Class<T> clientClass) {
        return clientClass.getDeclaringClass().getSimpleName();
    }
}
