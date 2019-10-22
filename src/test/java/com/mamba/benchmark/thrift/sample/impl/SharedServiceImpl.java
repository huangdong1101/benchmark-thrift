package com.mamba.benchmark.thrift.sample.impl;

import com.mamba.benchmark.thrift.sample.face.SharedService;
import com.mamba.benchmark.thrift.sample.face.SharedStructIn;
import com.mamba.benchmark.thrift.sample.face.SharedStructOut;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SharedServiceImpl implements SharedService.Iface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedServiceImpl.class);

    @Override
    public List<SharedStructOut> getStruct(int key, String token, SharedStructIn input) throws TException {
        System.out.println("Test ===> getStruct");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        SharedStructOut out = new SharedStructOut();
        out.setKey(10);
        out.setValue("vvvv");
        return Collections.singletonList(out);
    }

    @Override
    public void getStruct1(int key, String token, SharedStructIn input) throws TException {
        System.out.println("Test ===> getStruct1");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 9001;

        try (TNonblockingServerSocket socket = new TNonblockingServerSocket(port)) {
            LOGGER.info("=========Thrift server starting=======");
            LOGGER.info("Listen port: {}", port);

            SharedServiceImpl service = new SharedServiceImpl();
            SharedService.Processor processor = new SharedService.Processor(service);
            TNonblockingServer.Args arg = new TNonblockingServer.Args(socket);
            arg.protocolFactory(new TBinaryProtocol.Factory());
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
