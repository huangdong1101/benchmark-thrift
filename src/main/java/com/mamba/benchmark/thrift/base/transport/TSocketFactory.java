package com.mamba.benchmark.thrift.base.transport;

import com.google.common.net.HostAndPort;
import org.apache.thrift.transport.TSocket;

import java.util.Map;

public class TSocketFactory implements TTransportFactory<TSocket> {

    private final int socketTimeout;

    private final int connectTimeout;

    public TSocketFactory(int socketTimeout, int connectTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectTimeout = connectTimeout;
    }

    public TSocket getTransport(HostAndPort endpoint) {
        return new TSocket(endpoint.getHost(), endpoint.getPort(), this.socketTimeout, this.connectTimeout);
    }

    public static TSocketFactory newInstance(Map<String, String> attrs) {
        int timeout = toInt(attrs.get("timeout"), 0);
        int socketTimeout = toInt(attrs.get("socketTimeout"), timeout);
        int connectTimeout = toInt(attrs.get("connectTimeout"), timeout);
        return new TSocketFactory(socketTimeout, connectTimeout);
    }

    private static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(str);
        }
    }
}
