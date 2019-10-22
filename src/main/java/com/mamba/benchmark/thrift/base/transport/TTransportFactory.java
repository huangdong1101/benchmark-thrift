package com.mamba.benchmark.thrift.base.transport;

import com.google.common.net.HostAndPort;
import org.apache.thrift.transport.TTransport;

public interface TTransportFactory<T extends TTransport> {

    T getTransport(HostAndPort endpoint);
}
