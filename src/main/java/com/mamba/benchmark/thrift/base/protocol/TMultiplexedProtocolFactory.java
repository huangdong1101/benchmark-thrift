package com.mamba.benchmark.thrift.base.protocol;

import com.mamba.benchmark.thrift.base.TConfParser;
import org.apache.logging.log4j.util.Strings;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public class TMultiplexedProtocolFactory implements TProtocolFactory {

    private final TProtocolFactory protocolFactory;

    private final String serviceName;

    public TMultiplexedProtocolFactory(@Nonnull TProtocolFactory protocolFactory, @Nonnull String serviceName) {
        this.protocolFactory = Objects.requireNonNull(protocolFactory);
        this.serviceName = Objects.requireNonNull(serviceName);
    }

    @Override
    public TProtocol getProtocol(TTransport transport) {
        return new TMultiplexedProtocol(this.protocolFactory.getProtocol(transport), this.serviceName);
    }

    public static TMultiplexedProtocolFactory newInstance(@Nonnull Map<String, String> attrs) {
        String protocolAttr = attrs.get("protocol");
        String serviceNameAttr = attrs.get("serviceName");
        if (Strings.isBlank(protocolAttr)) {
            throw new IllegalArgumentException("'protocol' is required for TMultiplexedProtocol!");
        }
        if (Strings.isBlank(serviceNameAttr)) {
            throw new IllegalArgumentException("'serviceName' must be set for TMultiplexedProtocol!");
        }
        TProtocolFactory protocolFactory = TConfParser.parseProtocolFactory(protocolAttr);
        String serviceName = Objects.requireNonNull(serviceNameAttr);
        return new TMultiplexedProtocolFactory(protocolFactory, serviceName);
    }
}
