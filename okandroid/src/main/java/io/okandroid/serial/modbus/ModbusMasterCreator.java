package io.okandroid.serial.modbus;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

import java.io.InputStream;
import java.io.OutputStream;

import io.okandroid.serial.SerialDevice;

public class ModbusMasterCreator {
    public static ModbusMaster create(SerialDevice device) {
        return new ModbusFactory().createRtuMaster(new SerialPortWrapper() {
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
}
