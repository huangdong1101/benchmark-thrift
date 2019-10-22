package com.mamba.benchmark.thrift;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void test() throws Exception {
        String argsFile = this.getClass().getClassLoader().getResource("ut_arguments.txt").getFile();
        Main.main("-c", "1", "-t", "60",  "thrift_ut://0.0.0.0:9001/SharedService/getStruct?@".concat(argsFile));
        System.out.println(1);
    }
}
