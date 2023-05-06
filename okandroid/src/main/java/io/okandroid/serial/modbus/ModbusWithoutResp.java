package io.okandroid.serial.modbus;


import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;

import io.okandroid.exception.OkModbusException;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;

public class ModbusWithoutResp {
    // private SerialDevice device;
    private String filename;
    private ModbusMaster modbusMaster;

    public ModbusWithoutResp(String filename, ModbusMaster master) {
        this.filename = filename;
        this.modbusMaster = master;
        master.setRetries(0);
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
            emitter.onError(new OkModbusException("Too many requests, plz wait."));
        }
        return false;
    }

    public Single<OkModbusResponse> send(ModbusRequest request) {
        return Single.create(emitter -> {
            if (validIn(emitter)) {
                ModbusResponse response = modbusMaster.send(request);
                if (emitter != null && !emitter.isDisposed()) {
                    if (response.isException()) {
                        // exception
                        emitter.onError(new OkModbusException(response.getExceptionMessage()));
                    } else {
                        emitter.onSuccess(new OkModbusResponse(response, true));
                    }
                }
            }
        });
    }

    public ModbusMaster master() {
        return modbusMaster;
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
