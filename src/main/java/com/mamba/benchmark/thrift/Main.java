package com.mamba.benchmark.thrift;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.mamba.benchmark.common.executor.PressureExecutor;
import com.mamba.benchmark.common.pressure.Pressure;
import com.mamba.benchmark.thrift.conf.BenchmarkConf;
import com.mamba.benchmark.thrift.generator.InvariantTaskGenerator;
import com.mamba.benchmark.thrift.invocation.ServiceClientInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"-c"}, description = "concurrency")
    private String concurrency;

    @Parameter(names = {"-q"}, description = "throughput")
    private String throughput;

    @Parameter(names = {"-t"}, description = "timelimit", required = true)
    private int timelimit;

    @Parameter(names = {"-k"}, description = "keepAlive")
    private boolean keepAlive;

    @Parameter(description = "conf", required = true, converter = URLConverter.class)
    private BenchmarkConf conf;

    private void run() throws Exception {
        try (PressureExecutor executor = this.getExecutor()) {
            LOGGER.info("PressureExecutor will start in 1 second!");
            executor.start(1);
            LOGGER.info("PressureExecutor will stop in 10 second!");
            TimeUnit.SECONDS.sleep(10);
        }
        LOGGER.info("Thrift Benchmark Completed!");
    }

    private PressureExecutor<Runnable> getExecutor() {
        if (this.timelimit < 10) {
            throw new IllegalArgumentException("Invalid argument: timelimit=" + this.timelimit);
        }
        if (this.concurrency == null) {
            if (this.throughput == null) {
                throw new IllegalArgumentException("Invalid argument: concurrency is null, throughput is null");
            }
            Pressure pressure = Pressure.parse(this.throughput, this.timelimit);
            IntFunction<List<Runnable>> generator = this.getGenerator();
            return PressureExecutor.throughput(generator, pressure::currentQuantity);
        } else {
            if (this.throughput != null) {
                throw new IllegalArgumentException("Invalid argument: concurrency=" + this.concurrency + ", throughput=" + throughput);
            }
            Pressure pressure = Pressure.parse(this.concurrency, this.timelimit);
            IntFunction<List<Runnable>> generator = this.getGenerator();
            return PressureExecutor.concurrency(generator, pressure::currentQuantity);
        }
    }

    private IntFunction<List<Runnable>> getGenerator() {
        ServiceClientInvocation invocation = new ServiceClientInvocation(this.conf, this.keepAlive);
        List<String> arguments = this.conf.getArguments();
        if (arguments.isEmpty()) {
            return new InvariantTaskGenerator(invocation);
        } else {
            return new InvariantTaskGenerator(invocation, arguments.toArray(new String[arguments.size()]));
        }
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }

    private static class URLConverter implements IStringConverter<BenchmarkConf> {
        @Override
        public BenchmarkConf convert(String s) {
            try {
                return new BenchmarkConf(s);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
