package io.okandroid.serial.modbus;


import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.okandroid.exception.OkModbusException;
import io.okandroid.serial.SerialDevice;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;

public class Modbus {
    private SerialDevice device;
    private ModbusMaster modbusMaster;

    private Queue<OkModbusRequest> requestQueue = new LinkedBlockingQueue<>();

    public Modbus(SerialDevice serialDevice) {
        this.device = serialDevice;
        this.modbusMaster = new ModbusFactory().createRtuMaster(new SerialPortWrapper() {
            @Override
            public void close() throws Exception {
                device.close();
            }

            @Override
            public void open() throws Exception {
                device.open();
            }

            @Override
            public InputStream getInputStream() {
                return device.getInputStream();
            }

            @Override
            public OutputStream getOutputStream() {
                return device.getOutputStream();
            }

            @Override
            public int getBaudRate() {
                return device.getBaudRate();
            }

            @Override
            public int getDataBits() {
                return device.getDataBits();
            }

            @Override
            public int getStopBits() {
                return device.getStopBits();
            }

            @Override
            public int getParity() {
                return device.getParity();
            }
        });
    }

    public Single<ModbusResponse> sendQueued(ModbusRequest request) {
        return Single.create(emitter -> {
            requestQueue.add(new OkModbusRequest(request, emitter));
            continueRequestWorking();
        });
    }

    private synchronized void continueRequestWorking() {
        if (requestQueue.isEmpty()) return;
        OkModbusRequest request = requestQueue.poll();
        SingleEmitter<ModbusResponse> emitter = request.getEmitter();
        ModbusRequest modbusRequest = request.getRequest();
        try {
            Thread.sleep(50);
            ModbusResponse response = modbusMaster.send(modbusRequest);
            // 间隔 50 ms 执行下一条指令
            if (emitter != null && !emitter.isDisposed()) {
                if (response.isException()) {
                    // exception
                    emitter.onError(new OkModbusException(response.getExceptionMessage()));
                } else {
                    emitter.onSuccess(response);
                }
            }
        } catch (ModbusTransportException | InterruptedException e) {
            // e.printStackTrace();
            if (emitter != null && !emitter.isDisposed()) {
                emitter.onError(e);
            }
        }
        continueRequestWorking();
    }

    public ModbusMaster master() {
        return modbusMaster;
    }

    private static class OkModbusRequest {
        ModbusRequest request;
        SingleEmitter<ModbusResponse> emitter;

        public OkModbusRequest(ModbusRequest request, SingleEmitter<ModbusResponse> emitter) {
            this.request = request;
            this.emitter = emitter;
        }

        public ModbusRequest getRequest() {
            return request;
        }

        public SingleEmitter<ModbusResponse> getEmitter() {
            return emitter;
        }
    }
}
