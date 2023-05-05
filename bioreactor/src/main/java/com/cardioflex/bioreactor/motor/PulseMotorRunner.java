package com.cardioflex.bioreactor.motor;

public interface PulseMotorRunner {
    void exec(boolean isOn, boolean direction, float velocity);
}
