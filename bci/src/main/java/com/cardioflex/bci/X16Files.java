package com.cardioflex.bci;

import java.util.HashMap;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.service.BCIX16Service;

public class X16Files {
    private static HashMap<Long, BCIX16Service.X16DataPayload> bufferMap = new HashMap<>();
    private static int BUFFER_SIZE = 100;

    public static void append(BCIX16Service.X16DataPayload payload) {
        bufferMap.put(payload.data.get(0).timestamp, payload);
        if (bufferMap.size() > BUFFER_SIZE) {
            // store in file
            HashMap<Long, BCIX16Service.X16DataPayload> buffer = bufferMap;
            bufferMap = new HashMap<>();
            OkAndroid.newThread().scheduleDirect(() -> {
                
            });
        }
    }
}
