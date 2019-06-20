package com.mamba.benchmark.common.util;

import com.google.common.net.HostAndPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class BenchmarkUtilsTests {

    @Test
    void test_parseAddresses_0() {
        List<HostAndPort> addresses = BenchmarkUtils.parseAddresses("");
        Assertions.assertEquals(addresses.size(), 0);
    }

    @Test
    void test_parseAddresses_1() {
        List<HostAndPort> addresses = BenchmarkUtils.parseAddresses("127.0.0.1:8080");
        Assertions.assertEquals(addresses.size(), 1);
        Assertions.assertEquals(addresses.get(0).getHost(), "127.0.0.1");
        Assertions.assertEquals(addresses.get(0).getPort(), 8080);
    }

    @Test
    void test_parseAddresses_2() {
        List<HostAndPort> addresses = BenchmarkUtils.parseAddresses("127.0.0.1,127.0.0.2:8080");
        Assertions.assertEquals(addresses.size(), 2);
        Assertions.assertEquals(addresses.get(0).getHost(), "127.0.0.1");
        Assertions.assertEquals(addresses.get(0).getPort(), 8080);
        Assertions.assertEquals(addresses.get(1).getHost(), "127.0.0.2");
        Assertions.assertEquals(addresses.get(1).getPort(), 8080);
    }

    @Test
    void test_parseAddresses_3() {
        List<HostAndPort> addresses = BenchmarkUtils.parseAddresses("127.0.0.1,127.0.0.2:8080;127.0.0.1,127.0.0.2:8081");
        Assertions.assertEquals(addresses.size(), 4);
        Assertions.assertEquals(addresses.get(0).getHost(), "127.0.0.1");
        Assertions.assertEquals(addresses.get(0).getPort(), 8080);
        Assertions.assertEquals(addresses.get(1).getHost(), "127.0.0.2");
        Assertions.assertEquals(addresses.get(1).getPort(), 8080);
        Assertions.assertEquals(addresses.get(2).getHost(), "127.0.0.1");
        Assertions.assertEquals(addresses.get(2).getPort(), 8081);
        Assertions.assertEquals(addresses.get(3).getHost(), "127.0.0.2");
        Assertions.assertEquals(addresses.get(3).getPort(), 8081);
    }

    @Test
    void test_parseAddresses_e() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BenchmarkUtils.parseAddresses("127.0.0.1,127.0.0.2:8080;127.0.0.1,127.0.0.2"));
    }
}
