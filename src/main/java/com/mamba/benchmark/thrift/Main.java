package com.mamba.benchmark.thrift;

import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mamba.benchmark.common.executor.PressureExecutor;
import com.mamba.benchmark.common.pressure.Custom;
import com.mamba.benchmark.common.pressure.Fixed;
import com.mamba.benchmark.common.pressure.Gradient;
import com.mamba.benchmark.common.pressure.Pressure;
import com.mamba.benchmark.common.util.BenchmarkUtils;
import com.mamba.benchmark.thrift.client.DefaultClientFactory;
import com.mamba.benchmark.thrift.client.TClientFactory;
import com.mamba.benchmark.thrift.client.TTransportFactory;
import com.mamba.benchmark.thrift.define.Request;
import com.mamba.benchmark.thrift.generator.InvariantTaskGenerator;
import com.mamba.benchmark.thrift.tools.Tools;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Parameter(names = {"-idl"}, description = "IDL path", required = true)
    private File idl;

    @Parameter(names = {"-addr", "-address"}, description = "Server address", required = true)
    private String address;

    @Parameter(names = {"-req", "-request"}, description = "Request config path", required = true)
    private File request;

    @Parameter(names = {"-t"}, description = "throughput")
    private boolean throughput;

    @Parameter(names = {"-c"}, description = "concurrency")
    private boolean concurrency;

    @Parameter(names = {"-quantity"})
    private Integer quantity;

    @Parameter(names = {"-duration"})
    private Integer duration;

    @Parameter(names = {"-rampup", "-ramp-up"})
    private Integer rampup;

    @Parameter(names = {"-initialQuantity", "-initial-quantity"})
    private Integer initialQuantity;

    @Parameter(names = {"-finalQuantity", "-final-quantity"})
    private Integer finalQuantity;

    @Parameter(names = {"-incrementPerStep", "-increment-per-step"})
    private Integer incrementPerStep;

    @Parameter(names = {"-durationPerStep", "-duration-per-step"})
    private Integer durationPerStep;

    @Parameter(names = {"-quantities"})
    private List<String> quantities;

    private void run() throws Exception {
        try (PressureExecutor executor = this.getExecutor()) {
            LOGGER.info("PressureExecutor will start in 1 second!");
            executor.start(1);
            LOGGER.info("PressureExecutor will stop in 10 second!");
            TimeUnit.SECONDS.sleep(10);
        }
    }

    private PressureExecutor<Runnable> getExecutor() throws Exception {
        if (this.concurrency == this.throughput) {
            throw new IllegalArgumentException("Invalid argument: concurrency=" + this.concurrency + ", throughput=" + throughput);
        }
        boolean concurrency = this.concurrency;
        Pressure pressure = this.getPressure();
        IntFunction<List<Runnable>> generator = this.getGenerator();
        if (concurrency) {
            return PressureExecutor.concurrency(generator, pressure::currentQuantity);
        } else {
            return PressureExecutor.throughput(generator, pressure::currentQuantity);
        }
    }

    private IntFunction<List<Runnable>> getGenerator() throws Exception {
        JSONObject interfaceDescription = Tools.parseIDL(this.idl);
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{this.idl.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        String requestStr = Files.asCharSource(this.request, Charsets.UTF_8).read();
        Request request = Request.parse(classLoader, interfaceDescription, requestStr);
        TClientFactory<? extends TServiceClient> clientFactory = this.getClientFactory(request.getClientFactory());
        return InvariantTaskGenerator.newInstance(clientFactory, request);
    }

    private <T extends TServiceClient> TClientFactory getClientFactory(TServiceClientFactory<T> serviceClientFactory) {
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TTransportFactory transportFactory = new TTransportFactory(BenchmarkUtils.parseAddresses(this.address));
        return new DefaultClientFactory(serviceClientFactory, protocolFactory, transportFactory);
    }

    private Pressure getPressure() {
        if (this.quantity != null && this.duration != null) {
            if (this.rampup == null) {
                return new Fixed(this.quantity, this.duration);
            } else {
                return new Fixed(this.quantity, this.duration, this.rampup);
            }
        }
        if (this.initialQuantity != null && this.finalQuantity != null && this.incrementPerStep != null && this.durationPerStep != null) {
            return new Gradient(this.initialQuantity, this.finalQuantity, this.incrementPerStep, this.durationPerStep);
        }
        if (this.quantities != null && !this.quantities.isEmpty() && this.durationPerStep != null) {
            return new Custom(this.quantities.stream().mapToInt(Integer::parseInt).toArray(), this.durationPerStep);
        }
        throw new IllegalArgumentException("Invalid argument for init pressure");
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }
}
