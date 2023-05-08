package com.cardioflex.bioreactor.sys;

import com.cardioflex.bioreactor.motor.PulseMotorWorker;

import java.io.IOException;

public class Sys {
    public static void start() {
        // start motor by modbus
        try {
            PulseMotorWorker.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        PulseMotorWorker.stopAll();
    }
}
