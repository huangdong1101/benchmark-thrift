package com.mamba.benchmark.common.util;

import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.Splitter;
import com.google.common.net.HostAndPort;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BenchmarkUtils {

    public static <T> T cast(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (clazz == String.class) {
            return (T) obj.toString();
        }
        if (clazz == byte.class || clazz == Byte.class) {
            return (T) TypeUtils.castToByte(obj);
        }
        if (clazz == short.class || clazz == Short.class) {
            return (T) TypeUtils.castToShort(obj);
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) TypeUtils.castToInt(obj);
        }
        if (clazz == long.class || clazz == Long.class) {
            return (T) TypeUtils.castToLong(obj);
        }
        if (clazz == float.class || clazz == Float.class) {
            return (T) TypeUtils.castToFloat(obj);
        }
        if (clazz == double.class || clazz == Double.class) {
            return (T) TypeUtils.castToDouble(obj);
        }
        if (clazz == char.class || clazz == Character.class) {
            return (T) TypeUtils.castToChar(obj);
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) TypeUtils.castToBoolean(obj);
        }
        if (clazz == BigDecimal.class) {
            return (T) TypeUtils.castToBigDecimal(obj);
        }
        if (clazz == BigInteger.class) {
            return (T) TypeUtils.castToBigInteger(obj);
        }
        return TypeUtils.castToJavaBean(obj, clazz);
    }

    public static List<HostAndPort> parseAddresses(String addressesStr) {
        if (addressesStr == null || addressesStr.isEmpty()) {
            return Collections.emptyList();
        }
        List<HostAndPort> addresses = new LinkedList<>();
        try {
            Iterator<String> iterator = Splitter.on(';').split(addressesStr).iterator();
            while (iterator.hasNext()) {
                doParseAddresses(addresses, iterator.next());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid address: " + addressesStr, e);
        }
        return addresses;
    }

    private static void doParseAddresses(List<HostAndPort> addresses, String address) {
        int split = address.lastIndexOf(':');
        String hosts = address.substring(0, split);
        int port = Integer.parseInt(address.substring(split + 1));
        doParseAddresses(addresses, hosts, port);
    }

    private static void doParseAddresses(List<HostAndPort> addresses, String hosts, int port) {
        Iterator<String> iterator = Splitter.on(',').split(hosts).iterator();
        while (iterator.hasNext()) {
            addresses.add(HostAndPort.fromParts(iterator.next(), port));
        }
    }
}
