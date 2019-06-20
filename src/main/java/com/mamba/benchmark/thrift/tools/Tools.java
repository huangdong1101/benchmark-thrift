package com.mamba.benchmark.thrift.tools;

import com.alibaba.fastjson.JSONObject;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tools {

    private static final String CMD_FMT_TIDP = "java -jar " + Tools.class.getClassLoader().getResource("tools/TIDP.jar").getFile() + " %s";

    public static JSONObject parseIDL(File idl) {
        try {
            Process process = Runtime.getRuntime().exec(String.format(CMD_FMT_TIDP, idl.getAbsolutePath()));
            String content = CharStreams.toString(new InputStreamReader(process.getInputStream()));
            return JSONObject.parseObject(content);
        } catch (IOException e) {
            throw new IllegalStateException("Error Exec TIDP for " + idl.getAbsolutePath(), e);
        }
    }
}
