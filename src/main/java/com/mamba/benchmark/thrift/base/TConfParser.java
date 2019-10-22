package com.mamba.benchmark.thrift.base;

import com.mamba.benchmark.thrift.base.protocol.TMultiplexedProtocolFactory;
import com.mamba.benchmark.thrift.base.transport.TFramedTransportFactory;
import com.mamba.benchmark.thrift.base.transport.TSocketFactory;
import com.mamba.benchmark.thrift.base.transport.TTransportFactory;
import com.mamba.benchmark.common.util.ReflectUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class TConfParser {

    public static TTransportFactory parseTransportFactory(String define) {
        if (Strings.isBlank(define)) {
            throw new IllegalArgumentException("Transport define is blank!");
        }
        return parse(define, TConfParser::newTransportFactory);
    }

    public static TProtocolFactory parseProtocolFactory(String define) {
        if (Strings.isBlank(define)) {
            throw new IllegalArgumentException("Protocol define is blank!");
        }
        return parse(define, TConfParser::newProtocolFactory);
    }

    private static TTransportFactory newTransportFactory(String name, Map<String, String> attrs) {
        if (TSocket.class.getSimpleName().equals(name)) {
            return TSocketFactory.newInstance(attrs);
        }
        if (TFramedTransport.class.getSimpleName().equals(name)) {
            return TFramedTransportFactory.newInstance(attrs);
        }
        throw new IllegalArgumentException("Unsupported transport: " + name);
    }

    private static TProtocolFactory newProtocolFactory(String name, Map<String, String> attrs) {
        if (TBinaryProtocol.class.getSimpleName().equals(name)) {
            return new TBinaryProtocol.Factory();
        }
        if (TCompactProtocol.class.getSimpleName().equals(name)) {
            return new TCompactProtocol.Factory();
        }
        if (TTupleProtocol.class.getSimpleName().equals(name)) {
            return new TTupleProtocol.Factory();
        }
        if (TJSONProtocol.class.getSimpleName().equals(name)) {
            return new TJSONProtocol.Factory();
        }
        if (TSimpleJSONProtocol.class.getSimpleName().equals(name)) {
            return new TSimpleJSONProtocol.Factory();
        }
        if (TMultiplexedProtocol.class.getSimpleName().equals(name)) {
            return TMultiplexedProtocolFactory.newInstance(attrs);
        }
        throw new IllegalArgumentException();
    }

    public static void setField(Object obj, Map<String, String> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return;
        }
        for (Field field : obj.getClass().getDeclaredFields()) {
            String name = field.getName();
            int lastIndex = name.length() - 1;
            if (name.charAt(lastIndex) != '_') {
                continue;
            }
            String s = attrs.get(name.substring(0, lastIndex));
            if (s == null) {
                continue;
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                field.set(obj, ReflectUtils.cast(s, field.getGenericType()));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public static <T> T parse(String define, BiFunction<String, Map<String, String>, T> mapper) {
        int left = define.indexOf('(');
        if (left < 0) {
            return mapper.apply(trim(define, define.length()), Collections.emptyMap());
        }
        int right = define.lastIndexOf(')');
        if (right < left) {
            throw new IllegalArgumentException();
        }

        int split = 0;
        StringBuilder sb = new StringBuilder();
        Map<String, String> attrs = new HashMap<>();
        for (int i = left + 1, depth = 0; i < right; i++) {
            char ch = define.charAt(i);
            if (ch == ' ') {
                continue;
            }
            if (ch == ',') {
                if (depth == 0) {
                    putAttr(attrs, sb, split);
                    sb.setLength(0);
                    split = 0;
                    continue;
                }
            }
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth < 0) {
                    throw new IllegalArgumentException();
                }
            } else if (ch == '=') {
                if (split == 0) {
                    split = sb.length();
                }
            }
            sb.append(ch);
        }
        if (sb.length() > 0) {
            putAttr(attrs, sb, split);
        }
        return mapper.apply(trim(define, left), attrs);
    }

    private static String trim(String str, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < end; i++) {
            char ch = str.charAt(i);
            if (ch == ' ') {
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private static void putAttr(Map<String, String> attrs, StringBuilder sb, int split) {
        if (split <= 0) {
            throw new IllegalArgumentException();
        }
        attrs.put(sb.substring(0, split), sb.substring(split + 1));
    }
}

