package io.okandroid.serial.modbus;


import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.okandroid.exception.OkModbusException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;

public class Modbus {
    // private SerialDevice device;
    private String filename;
    private ModbusMaster modbusMaster;

    private Queue<OkModbusRequest> requestQueue = new LinkedBlockingQueue<>();
    private volatile boolean isWorking;

    public Modbus(String filename, ModbusMaster master) {
        this.filename = filename;
        this.modbusMaster = master;
    }

    public String getFilename() {
        return filename;
    }

    private long lastUpdateAt = System.currentTimeMillis();

    private boolean validIn(SingleEmitter<OkModbusResponse> emitter) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateAt > 2) {
            // if (requestQueue.size() < 4) {
            return true;
            // }
        }
        if (emitter != null && !emitter.isDisposed()) {
            emitter.onError(new OkModbusException(String.format("Too many requests enqueued [%d], plz wait.", requestQueue.size())));
        }
        return false;
    }

    public Single<OkModbusResponse> waitQueued(long millis) {
        return Single.create(emitter -> {
            if (!validIn(emitter)) {
                continueRequestWorking();
                return;
            }
            requestQueue.add(new OkModbusRequest(millis, emitter));
            continueRequestWorking();
        });
    }

    public Single<OkModbusResponse> sendQueued(ModbusRequest request) {
        return Single.create(emitter -> {
            if (!validIn(emitter)) {
                continueRequestWorking();
                return;
            }
            requestQueue.add(new OkModbusRequest(request, emitter));
            continueRequestWorking();
        });
    }

    public Single<OkModbusResponse> sendQueued(ModbusRequest request1, ModbusRequest request2) {
        return Single.create(emitter -> {
            if (!validIn(emitter)) {
                continueRequestWorking();
                return;
            }
            requestQueue.add(new OkModbusRequest(request1, emitter));
            requestQueue.add(new OkModbusRequest(request2, emitter));
            continueRequestWorking();
        });
    }

    private synchronized void continueRequestWorking() {
        // if (isWorking) return;
        if (requestQueue.isEmpty()) return;
        isWorking = true;
        OkModbusRequest request = requestQueue.poll();
        SingleEmitter<OkModbusResponse> emitter = request.getEmitter();
        ModbusRequest modbusRequest = request.getRequest();
        try {
            if (request.getMillis() > 0) {
                // sleep
                Thread.sleep(request.getMillis());
            }
            if (modbusRequest == null) {
                emitter.onSuccess(new OkModbusResponse(null, true));
                return;
            }
            // Thread.sleep(10); // 10ms is fine.
            ModbusResponse response = modbusMaster.send(modbusRequest);
            if (emitter != null && !emitter.isDisposed()) {
                if (response.isException()) {
                    // exception
                    emitter.onError(new OkModbusException(response.getExceptionMessage()));
                } else {
                    emitter.onSuccess(new OkModbusResponse(response, true));
                }
            }
        } catch (ModbusTransportException | InterruptedException e) {
            // e.printStackTrace();
            if (emitter != null && !emitter.isDisposed()) {
                emitter.onError(e);
            }
        }
        isWorking = false;
        continueRequestWorking();
    }

    public ModbusMaster master() {
        return modbusMaster;
    }

    private static class OkModbusRequest {
        ModbusRequest request;
        SingleEmitter<OkModbusResponse> emitter;

        long millis = 0; // if type is wait, millis > 0

        public OkModbusRequest(ModbusRequest request, SingleEmitter<OkModbusResponse> emitter) {
            this.request = request;
            this.emitter = emitter;
        }

        public OkModbusRequest(long millis, SingleEmitter<OkModbusResponse> emitter) {
            this.emitter = emitter;
            this.millis = millis;
        }

        public ModbusRequest getRequest() {
            return request;
        }

        public SingleEmitter<OkModbusResponse> getEmitter() {
            return emitter;
        }

        public long getMillis() {
            return millis;
        }
    }

    public static class OkModbusResponse {
        private ModbusResponse response;
        private boolean success;

        public OkModbusResponse(ModbusResponse response, boolean success) {
            this.response = response;
            this.success = success;
        }

        public ModbusResponse getResponse() {
            return response;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
