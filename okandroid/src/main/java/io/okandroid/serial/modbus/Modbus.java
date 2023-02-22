package io.okandroid.serial.modbus;


import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.serial.SerialPortWrapper;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.okandroid.serial.SerialDevice;

public class Modbus {
    private SerialDevice device;
    private ModbusMaster modbusMaster;

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

    public void send(ByteQueue queue) {
        try {
            ModbusResponse response = modbusMaster.send(ModbusRequest.createModbusRequest(queue));
            if (response.isException()) {
                // exception
                System.out.println(response.getExceptionMessage());
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    public ModbusMaster master() {
        return modbusMaster;
    }
}
