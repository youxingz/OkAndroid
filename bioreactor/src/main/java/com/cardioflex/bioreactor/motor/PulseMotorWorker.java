package com.cardioflex.bioreactor.motor;

import android.serialport.SerialPortFinder;

import com.serotonin.modbus4j.ModbusMaster;

import java.io.IOException;
import java.util.HashMap;

import io.okandroid.sensor.motor.Leadshine57PumpQueued;
import io.okandroid.serial.SerialDevice;
import io.okandroid.serial.modbus.ModbusMasterCreator;

public class PulseMotorWorker {
    private static final HashMap<String, PulseMotor> motors = new HashMap<>();

    /**
     * 初始化时需要进行重置，确保功能正常运行，该重置只需要执行一次即可
     * <p>
     * 后续需要将配置文件加载出来（从数据库），确保重启机器后能继续正常工作
     *
     * @throws IOException
     */
    public static void reset() throws IOException {
        String defaultDeviceName = new SerialPortFinder().getAllDevicesPath()[0]; // "/dev/ttyUSB0"
        SerialDevice device = SerialDevice.newBuilder(defaultDeviceName, 38400).dataBits(8).parity(0).stopBits(1).build();
        ModbusMaster modbusMaster = ModbusMasterCreator.create(device);
        modbusMaster.enableDebug(true);
        // stop all
        if (!motors.isEmpty())
            for (String key : motors.keySet()) {
                PulseMotor motor = motors.get(key);
                if (motor == null) continue;
                motor.stop();
            }
        motors.clear();
        motors.put("a1", new PulseMotor("a1", new Leadshine57PumpQueued(modbusMaster, 1)));
        motors.put("a2", new PulseMotor("a2", new Leadshine57PumpQueued(modbusMaster, 2)));
        motors.put("a3", new PulseMotor("a3", new Leadshine57PumpQueued(modbusMaster, 3)));
        motors.put("b1", new PulseMotor("b1", new Leadshine57PumpQueued(modbusMaster, 4)));
        motors.put("b2", new PulseMotor("b2", new Leadshine57PumpQueued(modbusMaster, 5)));
        motors.put("b3", new PulseMotor("b3", new Leadshine57PumpQueued(modbusMaster, 6)));
    }

    /**
     * 外部可以调用此方法进行配置更新
     *
     * @param tag
     * @param config
     */
    public static void updateConfig(String tag, PulseMotor.PulseMotorConfig config) {
        PulseMotor motor = motors.get(tag);
        if (motor == null) return;
        motor.setConfig(config);
        if (config.getIsOn() != null && config.getIsOn()) {
            motor.start();
        } else {
            motor.stop();
        }
    }
}
