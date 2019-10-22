package com.mamba.benchmark.thrift.base.client;

import com.google.common.net.HostAndPort;
import com.mamba.benchmark.thrift.base.transport.TTransportFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedServiceClientFactory<T extends TServiceClient> extends BasicServiceClientFactory<T> implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedServiceClientFactory.class);

    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

    private final AtomicInteger[] counters = {
            new AtomicInteger(), //total
            new AtomicInteger(), //active
            new AtomicInteger()  //closed
    };

    public CachedServiceClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory, List<HostAndPort> endpoints) {
        super(serviceClientFactory, protocolFactory, transportFactory, endpoints);
    }

    public CachedServiceClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory, List<HostAndPort> endpoints, GenericObjectPoolConfig<T> poolConfig) {
        super(serviceClientFactory, protocolFactory, transportFactory, endpoints);
    }

    @Override
    public T getClient() throws TTransportException {
        T client = this.queue.poll();
        if (client == null) {
            client = super.getClient();
            this.counters[0].incrementAndGet();
        }
        this.counters[1].incrementAndGet();
        this.printCounters(this::printCounters);
        return client;
    }

    private void printCounters() {
        LOGGER.info("Clients counter => total: {}, active: {}, closed: {}, queue: {}", this.counters[0].get(), this.counters[1].get(), this.counters[2].get(), this.queue.size());
    }

    @Override
    public void close(T client) {
        if (!this.queue.offer(client)) {
            this.counters[2].incrementAndGet();
            super.close(client);
        }
        this.counters[1].decrementAndGet();
    }

    @Override
    public void close() {
        while (!this.queue.isEmpty()) {
            T client = this.queue.poll();
            if (client != null) {
                super.close(client);
            }
        }
    }
}
