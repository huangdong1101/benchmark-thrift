package com.mamba.benchmark.thrift.generator;

import com.mamba.benchmark.thrift.invocation.ServiceClientInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class InvariantTaskGenerator implements IntFunction<List<Runnable>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvariantTaskGenerator.class);

    private final ServiceClientInvocation invocation;

    private final Object[] arguments;

    public InvariantTaskGenerator(ServiceClientInvocation invocation, String... arguments) {
        this.invocation = invocation;
        this.arguments = invocation.parseArgs(arguments);
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
        this.invocation.invoke(this.arguments);
    }
}
