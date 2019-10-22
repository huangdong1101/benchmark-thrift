package com.mamba.benchmark.thrift.conf;

import com.mamba.benchmark.common.JarFileLoader;
import com.mamba.benchmark.thrift.base.TConfParser;
import com.mamba.benchmark.thrift.base.transport.TTransportFactory;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.apache.thrift.protocol.TProtocolFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
public class ThriftConf implements Closeable {

    private final JarFileLoader classLoader;

    private final TProtocolFactory protocolFactory;

    private final TTransportFactory transportFactory;

    public ThriftConf(File file) {
        if (file == null) {
            throw new NullPointerException("Thrift conf file is null!");
        }
        if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException("Invalid thrift conf file: " + file.getPath());
        }
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new IllegalStateException("Load thrift conf error: " + e.getMessage(), e);
        }
        String classpath = properties.getProperty("classpath");
        if (Strings.isBlank(classpath)) {
            throw new IllegalStateException("Blank 'classpath' in thrift conf");
        }

        File jarFile = (classpath.charAt(0) == File.separatorChar) ? new File(classpath) : new File(file.getParent(), classpath);
        try {
            this.classLoader = new JarFileLoader(jarFile);
        } catch (IOException e) {
            throw new IllegalStateException("Error 'classpath' in thrift conf: " + classpath, e);
        }

        String protocol = properties.getProperty("protocol");
        if (Strings.isBlank(protocol)) {
            throw new IllegalStateException("Blank 'protocol' in thrift conf");
        }
        try {
            this.protocolFactory = TConfParser.parseProtocolFactory(protocol);
        } catch (Exception e) {
            throw new IllegalStateException("Error 'protocol' in thrift conf: " + protocol, e);
        }
        if (this.protocolFactory == null) {
            throw new IllegalStateException("Error 'protocol' in thrift conf: " + protocol);
        }

        String transport = properties.getProperty("transport");
        if (Strings.isBlank(protocol)) {
            throw new IllegalStateException("Blank 'transport' in thrift conf");
        }
        try {
            this.transportFactory = TConfParser.parseTransportFactory(transport);
        } catch (Exception e) {
            throw new IllegalStateException("Error 'transport' in thrift conf: " + transport, e);
        }
        if (this.transportFactory == null) {
            throw new IllegalStateException("Error 'transport' in thrift conf: " + transport);
        }
    }

    @Override
    public void close() throws IOException {
        this.classLoader.close();
    }
}
