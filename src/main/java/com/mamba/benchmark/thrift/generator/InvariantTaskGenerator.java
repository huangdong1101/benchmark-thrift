package com.mamba.benchmark.thrift.generator;

import com.mamba.benchmark.thrift.define.Request;
import com.mamba.benchmark.thrift.reflect.Invoker;
import com.mamba.benchmark.thrift.client.TClientFactory;
import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class InvariantTaskGenerator<T extends TServiceClient> implements IntFunction<List<Runnable>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvariantTaskGenerator.class);

    private final Invoker<T> invoker;

    private final Method method;

    private final Object[] arguments;

    private InvariantTaskGenerator(Invoker invoker, Method method, Object... arguments) {
        this.invoker = invoker;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public List<Runnable> apply(int num) {
        List<Runnable> tasks = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            tasks.add(this::execute);
        }
        return tasks;
    }

    private void execute() {
        this.invoker.invoke(this.method, this.arguments);
    }

    public static <T extends TServiceClient> InvariantTaskGenerator newInstance(TClientFactory<T> clientFactory, Request request) {
        Invoker<T> invoker = new Invoker<>(clientFactory);
        return new InvariantTaskGenerator(invoker, request.getMethod(), request.getArguments());
    }
}
