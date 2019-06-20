package com.mamba.benchmark.thrift;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void test() throws Exception {
        Main.main("-idl", MainTest.class.getClassLoader().getResource("sample.jar").getFile(),
                "-addr", "0.0.0.0:9001",
                "-req", MainTest.class.getClassLoader().getResource("request.json").getFile(),
                "-t",
                "-quantity", "10",
                "-duration", "60"
        );
        System.out.println(1);
    }
}
