package com.mamba.benchmark.thrift.base.client;

import com.google.common.net.HostAndPort;
import com.mamba.benchmark.thrift.base.transport.TTransportFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class BasicServiceClientFactory<T extends TServiceClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicServiceClientFactory.class);

    private final TServiceClientFactory<T> serviceClientFactory;

    private final TProtocolFactory protocolFactory;

    private final TTransportFactory transportFactory;

    private final Supplier<HostAndPort> endpoint;

    private final AtomicInteger timestamp = new AtomicInteger();

    private final AtomicInteger[] counters = {
            new AtomicInteger(), //open
            new AtomicInteger()  //close
    };

    public BasicServiceClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory, List<HostAndPort> endpoints) {
        this.serviceClientFactory = Objects.requireNonNull(serviceClientFactory);
        this.protocolFactory = Objects.requireNonNull(protocolFactory);
        this.transportFactory = Objects.requireNonNull(transportFactory);
        if (endpoints == null || endpoints.isEmpty()) {
            throw new IllegalArgumentException("");
        }
        if (endpoints.size() == 1) {
            this.endpoint = new SingleEndpointSupplier(endpoints.get(0));
        } else {
            this.endpoint = new MultiEndpointsSupplier(endpoints.toArray(new HostAndPort[endpoints.size()]));
        }
    }

    public T getClient() throws TTransportException {
        TTransport transport = this.transportFactory.getTransport(this.endpoint.get());
        if (!transport.isOpen()) {
            transport.open();
        }
        TProtocol protocol = this.protocolFactory.getProtocol(transport);
        this.printCounters(this::printCounters);
        return this.serviceClientFactory.getClient(protocol);
    }

//    public void open(T client) throws TTransportException {
//        TProtocol iprot = client.getInputProtocol();
//        TTransport itrans = iprot.getTransport();
//        if (!itrans.isOpen()) {
//            itrans.open();
//        }
//        TProtocol oprot = client.getOutputProtocol();
//        if (oprot == iprot) {
//            return;
//        }
//        TTransport otrans = oprot.getTransport();
//        if (otrans == itrans) {
//            return;
//        }
//        if (!otrans.isOpen()) {
//            otrans.open();
//        }
//    }

    public void close(T client) {
        TProtocol iprot = client.getInputProtocol();
        TProtocol oprot = client.getOutputProtocol();
        if (oprot == iprot) {
            iprot.getTransport().close();
            return;
        }
        TTransport itrans = iprot.getTransport();
        TTransport otrans = oprot.getTransport();
        if (otrans == itrans) {
            itrans.close();
            return;
        }
        try {
            itrans.close();
            otrans.close();
        } finally {
            otrans.close();
        }
    }

    protected void printCounters(Runnable runnable) {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        int timestamp = this.timestamp.get();
        if (currentTime > timestamp && this.timestamp.compareAndSet(timestamp, currentTime)) {
            runnable.run();
        }
    }

    private void printCounters() {
        LOGGER.info("Clients counter => open: {}, close: {}", this.counters[0].get(), this.counters[1].get());
    }

    private static class SingleEndpointSupplier implements Supplier<HostAndPort> {

        private final HostAndPort endpoint;

        public SingleEndpointSupplier(HostAndPort endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public HostAndPort get() {
            return this.endpoint;
        }
    }

    private static class MultiEndpointsSupplier implements Supplier<HostAndPort> {

        private final HostAndPort[] endpoints;

        private final AtomicInteger sequence = new AtomicInteger(0);

        public MultiEndpointsSupplier(HostAndPort[] endpoints) {
            this.endpoints = endpoints;
        }

        @Override
        public HostAndPort get() {
            return this.endpoints[this.sequence.getAndIncrement() % this.endpoints.length];
        }
    }
}
