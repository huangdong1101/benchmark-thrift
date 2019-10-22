package com.mamba.benchmark.thrift.base.transport;

import com.google.common.net.HostAndPort;
import com.mamba.benchmark.thrift.base.TConfParser;
import org.apache.logging.log4j.util.Strings;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class TFramedTransportFactory implements TTransportFactory<TFramedTransport> {

    private final TTransportFactory transportFactory;

    private final TFramedTransport.Factory framedTransportFactory;

    public TFramedTransportFactory(@Nonnull TTransportFactory transportFactory, @Nonnull TFramedTransport.Factory framedTransportFactory) {
        this.transportFactory = Objects.requireNonNull(transportFactory);
        this.framedTransportFactory = Objects.requireNonNull(framedTransportFactory);
    }

    public TFramedTransport getTransport(HostAndPort endpoint) {
        TTransport transport = this.transportFactory.getTransport(endpoint);
        return (TFramedTransport) this.framedTransportFactory.getTransport(transport);
    }

    public static TFramedTransportFactory newInstance(Map<String, String> attrs) {
        String transportAttr = attrs.get("transport");
        if (Strings.isBlank(transportAttr)) {
            throw new IllegalArgumentException("'transport' is required for TFramedTransportFactory!");
        }
        TTransportFactory transportFactory = TConfParser.parseTransportFactory(transportAttr);
        TFramedTransport.Factory framedTransportFactory = new TFramedTransport.Factory();
        TConfParser.setField(framedTransportFactory, attrs);
        return new TFramedTransportFactory(transportFactory, framedTransportFactory);
    }
}
