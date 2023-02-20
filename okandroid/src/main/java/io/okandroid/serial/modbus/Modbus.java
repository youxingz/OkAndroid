package io.okandroid.serial.modbus;

import android.serialport.SerialPort;

import java.io.IOException;

public class Modbus {
    {
        try {
            SerialPort port = SerialPort.newBuilder("dev/1", 9600).build();
            port.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
