package com.mamba.benchmark.thrift.client;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

public abstract class TClientFactory<T extends TServiceClient> {

    private final TServiceClientFactory<T> serviceClientFactory;

    private final TProtocolFactory protocolFactory;

    private final TTransportFactory transportFactory;

    public TClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory) {
        this.serviceClientFactory = serviceClientFactory;
        this.protocolFactory = protocolFactory;
        this.transportFactory = transportFactory;
    }

    public abstract T getClient() throws Exception;

    protected T getClient0() {
        TTransport transport = this.transportFactory.getTransport();
        TProtocol protocol = this.protocolFactory.getProtocol(transport);
        return this.serviceClientFactory.getClient(protocol);
    }

    public abstract void close(T client);
}
