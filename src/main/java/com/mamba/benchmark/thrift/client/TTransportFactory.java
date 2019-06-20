package com.mamba.benchmark.thrift.client;

import com.google.common.net.HostAndPort;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TTransportFactory {

    private final AtomicInteger sequence = new AtomicInteger(0);

    private final HostAndPort[] addresses;

//    public TTransportFactory(HostAndPort... addresses) {
//        if (addresses == null || addresses.length < 1) {
//            throw new IllegalArgumentException("Empty addresses");
//        }
//        this.addresses = addresses;
//    }

    public TTransportFactory(List<HostAndPort> addresses) {
        if (addresses == null || addresses.size() < 1) {
            throw new IllegalArgumentException("Empty addresses");
        }
        this.addresses = addresses.toArray(new HostAndPort[addresses.size()]);
    }

    public TTransport getTransport() {
        HostAndPort address = this.getAddress();
        //TODO
        return new TFramedTransport(new TSocket(address.getHost(), address.getPort()));
    }

    private HostAndPort getAddress() {
        int size = this.addresses.length;
        if (size == 1) {
            return this.addresses[0];
        } else {
            return this.addresses[this.sequence.getAndIncrement() % size];
        }
    }
}
