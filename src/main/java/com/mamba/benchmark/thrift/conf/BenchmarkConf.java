package com.mamba.benchmark.thrift.conf;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.net.HostAndPort;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class BenchmarkConf {

    private final List<HostAndPort> endpoints;

    private final String service;

    private final String method;

    private final List<String> arguments;

    private final ThriftConf thriftConf;

    public BenchmarkConf(String str) throws IOException {
        if (str == null) {
            throw new NullPointerException();
        }
        int pe = str.indexOf("://");  //end of protocol
        if (pe < 0) {
            throw new IllegalArgumentException("Invalid URL: " + str);
        }
        String tmp;
        int qb = str.indexOf('?', pe + 3); //begin of query
        if (qb == -1) { //Empty Arguments
            tmp = str.substring(pe + 3);
            this.arguments = Collections.emptyList();
        } else {
            tmp = str.substring(pe + 3, qb);
            String argumentsStr = str.substring(qb + 1);
            this.arguments = Collections.unmodifiableList(parseArguments(argumentsStr));
        }

        String thriftConfStr = str.substring(0, pe);
        URL thriftConfURL = Thread.currentThread().getContextClassLoader().getResource(MessageFormat.format("conf/{0}.cfg", thriftConfStr));
        if (thriftConfURL == null) {
            throw new IllegalArgumentException("Invalid URL: " + str);
        }
        this.thriftConf = new ThriftConf(new File(thriftConfURL.getFile()));

        List<String> shards = Splitter.on('/').splitToList(tmp);
        if (shards.size() != 3) {
            throw new IllegalArgumentException("Invalid URL: " + str);
        }
        this.endpoints = parseEndpoints(shards.get(0));
        this.service = shards.get(1);
        this.method = shards.get(2);
    }

    private static List<HostAndPort> parseEndpoints(String str) {
        List<HostAndPort> addresses = new ArrayList<>();
        Splitter.on(';').split(str).forEach(s -> {
            int split = s.lastIndexOf(':');
            if (split == -1) { //缺省端口
                Splitter.on(',').split(s).forEach(host -> addresses.add(HostAndPort.fromHost(host)));
            } else {
                int port = Integer.parseInt(s.substring(split + 1));
                String hosts = s.substring(0, split);
                Splitter.on(',').split(hosts).forEach(host -> addresses.add(HostAndPort.fromParts(host, port)));
            }
        });
        return addresses;
    }

    private static List<String> parseArguments(String str) throws IOException {
        if (str.charAt(0) == '@') {
            File argumentsFile = new File(str.substring(1));
            return Files.readLines(argumentsFile, Charsets.UTF_8);
        } else {
            return Splitter.on('&').splitToList(str);
        }
    }
}
