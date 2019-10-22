package com.mamba.benchmark.thrift.sample.impl;

import com.mamba.benchmark.thrift.sample.face.SharedService;
import com.mamba.benchmark.thrift.sample.face.SharedStructIn;
import com.mamba.benchmark.thrift.sample.face.SharedStructOut;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SharedServiceImpl implements SharedService.Iface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedServiceImpl.class);

    @Override
    public List<SharedStructOut> getStruct(int key, String token, SharedStructIn input) throws TException {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        SharedStructOut out = new SharedStructOut();
        out.setKey(10);
        out.setValue("vvvv");
        return Collections.singletonList(out);
    }


    public static void main(String[] args) throws Exception {
        int port = 9001;

        try (TNonblockingServerSocket socket = new TNonblockingServerSocket(port)) {
            LOGGER.info("=========Thrift server starting=======");
            LOGGER.info("Listen port: {}", port);

            SharedServiceImpl service = new SharedServiceImpl();
            SharedService.Processor processor = new SharedService.Processor(service);
            TNonblockingServer.Args arg = new TNonblockingServer.Args(socket);
            arg.protocolFactory(new TCompactProtocol.Factory());
            arg.transportFactory(new TFramedTransport.Factory());
            arg.processorFactory(new TProcessorFactory(processor));
            TServer server = new TNonblockingServer(arg);

            LOGGER.info("=========Thrift server started=======");
            server.serve();
            LOGGER.error("Thrift server stopped as an error happened");
            server.stop();
        }
        System.exit(1);
    }
}
