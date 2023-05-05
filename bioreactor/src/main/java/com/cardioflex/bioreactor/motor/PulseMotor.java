package com.cardioflex.bioreactor.motor;

import io.okandroid.OkAndroid;

public class PulseMotor {
    private String tag; // a1, a2, a3, b1, b2, b3
    // current:
    private float velocity;
    private boolean direction;

    // config:
    private PulseMotorConfig config;
    private PulseMotorRunner motorRunner;
    private volatile boolean working;


    public void setConfig(PulseMotorConfig config) {
        this.config = config;
    }

    public void start() {
        OkAndroid.newThread().scheduleDirect(() -> {
            stop();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            working = true;
            while (working) {
                try {
                    // modbus
                    // period1
                    direction = config.getD1();
                    velocity = config.getV1();
                    motorRunner.exec(true, direction, velocity);
                    Thread.sleep(config.getT1().longValue());
                    // period2
                    direction = config.getD2();
                    velocity = config.getV2();
                    motorRunner.exec(true, direction, velocity);
                    Thread.sleep(config.getT2().longValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        working = false;
    }

    public static class PulseMotorConfig {
        private Boolean isOn;
        private Boolean d1;
        private Float v1;
        private Float t1;
        private Boolean d2;
        private Float v2;
        private Float t2;

        public PulseMotorConfig() {
        }

        public PulseMotorConfig(Boolean isOn, Boolean d1, Float v1, Float t1, Boolean d2, Float v2, Float t2) {
            this.isOn = isOn;
            this.d1 = d1;
            this.v1 = v1;
            this.t1 = t1;
            this.d2 = d2;
            this.v2 = v2;
            this.t2 = t2;
        }

        public Boolean getOn() {
            return isOn;
        }

        public void setOn(Boolean on) {
            isOn = on;
        }

        public Boolean getD1() {
            return d1;
        }

        public void setD1(Boolean d1) {
            this.d1 = d1;
        }

        public Float getV1() {
            return v1;
        }

        public void setV1(Float v1) {
            this.v1 = v1;
        }

        public Float getT1() {
            return t1;
        }

        public void setT1(Float t1) {
            this.t1 = t1;
        }

        public Boolean getD2() {
            return d2;
        }

        public void setD2(Boolean d2) {
            this.d2 = d2;
        }

        public Float getV2() {
            return v2;
        }

        public void setV2(Float v2) {
            this.v2 = v2;
        }

        public Float getT2() {
            return t2;
        }

        public void setT2(Float t2) {
            this.t2 = t2;
        }
    }
}
