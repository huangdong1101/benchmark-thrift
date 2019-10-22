package com.mamba.benchmark.thrift.invocation;

import com.mamba.benchmark.common.util.ReflectUtils;
import com.mamba.benchmark.thrift.base.client.BasicServiceClientFactory;
import com.mamba.benchmark.thrift.base.client.CachedServiceClientFactory;
import com.mamba.benchmark.thrift.conf.BenchmarkConf;
import com.mamba.benchmark.thrift.conf.ThriftConf;
import org.apache.logging.log4j.util.Strings;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceClientInvocation<T extends TServiceClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClientInvocation.class);

    private final String api;

    private final Method method;

    private final BasicServiceClientFactory<T> serviceClientFactory;

    public ServiceClientInvocation(BenchmarkConf config, boolean keepAlive) {
        ThriftConf thriftConf = config.getThriftConf();

        this.api = String.format("%s.%s", config.getService(), config.getMethod());

        Class<T> serviceClientClass;
        try {
            serviceClientClass = (Class<T>) thriftConf.getClassLoader().loadClass(config.getService().concat("$Client"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid service: " + config.getService(), e);
        }

        try {
            this.method = ReflectUtils.findMethod(serviceClientClass, config.getMethod());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Invalid method: " + config.getMethod(), e);
        }

        TServiceClientFactory<T> serviceClientFactory;
        try {
            serviceClientFactory = (TServiceClientFactory<T>) ReflectUtils.findInnerClass(serviceClientClass, "Factory").newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Invalid serviceClient: " + serviceClientClass.getName(), e);
        }

        if (keepAlive) {
            this.serviceClientFactory = new BasicServiceClientFactory<>(serviceClientFactory, thriftConf.getProtocolFactory(), thriftConf.getTransportFactory(), config.getEndpoints());
        } else {
            this.serviceClientFactory = new CachedServiceClientFactory<>(serviceClientFactory, thriftConf.getProtocolFactory(), thriftConf.getTransportFactory(), config.getEndpoints());
        }
    }

    public void invoke(Object... args) {
        T client;
        try {
            client = this.serviceClientFactory.getClient();
        } catch (Exception e) {
            LOGGER.info("{} Error1: {} {}", this.api, e.getMessage());
            return;
        }
        try {
            this.doInvoke(client, args);
        } finally {
            this.serviceClientFactory.close(client);
        }
    }

    private void doInvoke(T client, Object... args) {
        long beginTime = System.currentTimeMillis();
        try {
            LOGGER.info("{} Send", this.api);
            this.method.invoke(client, args);
            long endTime = System.currentTimeMillis();
            LOGGER.info("{} Response {} ms, success", this.api, (endTime - beginTime));
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof TException) {
                if (t instanceof TBase) {
                    long endTime = System.currentTimeMillis();
                    LOGGER.info("{} Response {} ms, error : {}", this.api, (endTime - beginTime), t.toString());
                } else if (t instanceof TTransportException) {
                    LOGGER.info("{} Error0: TTransportException {} {}", this.api, ((TTransportException) t).getType(), t.getMessage());
                } else if (t instanceof TProtocolException) {
                    LOGGER.info("{} Error0: TProtocolException {} {}", this.api, ((TProtocolException) t).getType(), t.getMessage());
                } else if (t instanceof TApplicationException) {
                    LOGGER.info("{} Error0: TApplicationException {} {}", this.api, ((TApplicationException) t).getType(), t.getMessage());
                } else {
                    LOGGER.info("{} Error0: {} {}", this.api, t.getClass().getSimpleName(), t.getMessage());
                }
            } else {
                LOGGER.info("{} Error2: {} {}", this.api, t.getClass().getSimpleName(), t.getMessage());
            }
        } catch (IllegalAccessException e) {
            LOGGER.info("{} Error2: {} {}", this.api, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public Object[] parseArgs(String... args) {
        if (args == null || args.length == 0) {
            return doParseArgs(this.method);
        }
        int parameterCount = this.method.getParameterCount();
        if (args.length <= parameterCount) {
            return doParseArgs(this.method, args);
        }

        for (int i = parameterCount; i < args.length; i++) {
            if (!Strings.isBlank(args[i])) {
                throw new IllegalArgumentException("Invalid args. expect parameter count: " + parameterCount + ", actual args size: " + args.length);
            }
        }
        return doParseArgs(this.method, args);
    }

    private static Object[] doParseArgs(Method method, String... args) {
        try {
            return ReflectUtils.castArgs(method, args);
        } catch (Exception e) {
            throw new IllegalStateException("Error args: " + e.getMessage(), e);
        }
    }
}
