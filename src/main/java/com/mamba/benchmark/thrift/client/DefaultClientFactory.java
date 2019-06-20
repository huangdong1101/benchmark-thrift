package com.mamba.benchmark.thrift.client;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransportException;

public class DefaultClientFactory<T extends TServiceClient> extends TClientFactory<T> {

    public DefaultClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory) {
        super(serviceClientFactory, protocolFactory, transportFactory);
    }

    @Override
    public T getClient() throws TTransportException {
        T client = this.getClient0();
        TServiceClientUtils.open(client);
        return client;
    }

    @Override
    public void close(T client) {
        TServiceClientUtils.close(client);
    }
}
