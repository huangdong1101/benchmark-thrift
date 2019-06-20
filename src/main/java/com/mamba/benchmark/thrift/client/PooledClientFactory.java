package com.mamba.benchmark.thrift.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TProtocolFactory;

public class PooledClientFactory<T extends TServiceClient> extends TClientFactory<T> {

    private final GenericObjectPool<T> pool;

    public PooledClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory) {
        super(serviceClientFactory, protocolFactory, transportFactory);
        this.pool = new GenericObjectPool(new TPooledObjectFactory(super::getClient0));
    }

    public PooledClientFactory(TServiceClientFactory<T> serviceClientFactory, TProtocolFactory protocolFactory, TTransportFactory transportFactory, GenericObjectPoolConfig<T> poolConfig) {
        super(serviceClientFactory, protocolFactory, transportFactory);
        this.pool = new GenericObjectPool(new TPooledObjectFactory(super::getClient0), poolConfig);
    }

    @Override
    public T getClient() throws Exception {
        return this.pool.borrowObject();
    }

    @Override
    public void close(T client) {
        if (TServiceClientUtils.isOpen(client)) {
            this.pool.returnObject(client);
        }
    }

    private static class TPooledObjectFactory<T extends TServiceClient> implements PooledObjectFactory<T> {

        private final TObjectFactory<T> factory;

        public TPooledObjectFactory(TObjectFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public PooledObject<T> makeObject() throws Exception {
            return new DefaultPooledObject(this.factory.makeObject());
        }

        @Override
        public void destroyObject(PooledObject<T> pooledObject) {
            TServiceClientUtils.close(pooledObject.getObject());
        }

        @Override
        public boolean validateObject(PooledObject<T> pooledObject) {
            return TServiceClientUtils.isOpen(pooledObject.getObject());
        }

        @Override
        public void activateObject(PooledObject<T> pooledObject) throws Exception {
            TServiceClientUtils.open(pooledObject.getObject());
        }

        @Override
        public void passivateObject(PooledObject<T> pooledObject) {
        }
    }

    private interface TObjectFactory<T extends TServiceClient> {

        T makeObject() throws Exception;
    }
}
