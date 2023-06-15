package com.cardioflex.bioreactor.motor;

import com.cardioflex.bioreactor.CoreActivity;
import com.cardioflex.bioreactor.sys.Sys;
import com.cardioflex.bioreactor.x.EventPayload;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.okandroid.OkAndroid;
import io.okandroid.http.OkHttpHelper;
import io.okandroid.js.EventResponse;
import io.okandroid.sensor.motor.Leadshine57PumpQueued;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.Response;

public class PulseMotor {
    private static final String STREAM_NAME = "pulse-motor-stream";
    private static final HashMap<String, String> ipStore = new HashMap<>();

    static {
        ipStore.put("a1", "10.168.1.4");
        ipStore.put("a2", "10.168.1.4");
        ipStore.put("a3", "10.168.1.4");
        ipStore.put("b1", "10.168.1.4");
        ipStore.put("b2", "10.168.1.4");
        ipStore.put("b3", "10.168.1.4");
    }

    private String tag; // a1, a2, a3, b1, b2, b3
    private String ip;
    // current:
    private volatile float velocity;
    private volatile boolean direction;

    // config:
    private volatile PulseMotorConfig config;
    private volatile boolean working;

    private Leadshine57PumpQueued motor;
    private Disposable motorDisposable;

    public PulseMotor(String tag, Leadshine57PumpQueued modbus) {
        this.tag = tag;
        this.ip = ipStore.get(tag);
        if (ip == null) {
            throw new IllegalArgumentException("Tag is invalid.");
        }
        this.motor = modbus;
    }

    public void setConfig(PulseMotorConfig config) {
        this.config = config;
    }

    public PulseMotorConfig getConfig() {
        return this.config;
    }

    public void start() {
        OkAndroid.newThread().scheduleDirect(() -> {
            stop();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            working = config.getIsOn() != null && config.getIsOn();
            if (!working) {
                return;
            }
            try {
                sendHttp(true);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            while (working) {
                try {
                    // http
                    // period1
                    direction = config.getD1();
                    velocity = config.getV1();
                    // motor.changeVelocity((int) velocity);
                    // motor.changeDirection(direction);
                    if (config.getT1().longValue() > 100) {
                        notify(true, direction, velocity);
                    }
                    Thread.sleep(config.getT1().longValue());
                    // period2
                    direction = config.getD2();
                    velocity = config.getV2();
                    // motor.changeVelocity((int) velocity);
                    // motor.changeDirection(direction);
                    if (config.getT2().longValue() > 100) {
                        notify(true, direction, velocity);
                    }
                    Thread.sleep(config.getT2().longValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stop();
            notify(false, direction, 0f);
        });
    }

    // public void start2() {
    //     OkAndroid.newThread().scheduleDirect(() -> {
    //         stop();
    //         try {
    //             Thread.sleep(500);
    //         } catch (InterruptedException e) {
    //             throw new RuntimeException(e);
    //         }
    //         working = config.getIsOn() != null && config.getIsOn();
    //         if (!working) {
    //             return;
    //         }
    //         try {
    //             motor.turnOn().subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new Observer<Integer>() {
    //                 @Override
    //                 public void onSubscribe(@NonNull Disposable d) {
    //                     motorDisposable = d;
    //                 }
    //
    //                 @Override
    //                 public void onNext(@NonNull Integer speed) {
    //                     // 变速
    //                 }
    //
    //                 @Override
    //                 public void onError(@NonNull Throwable e) {
    //                     e.printStackTrace();
    //                 }
    //
    //                 @Override
    //                 public void onComplete() {
    //
    //                 }
    //             });
    //         } catch (ModbusTransportException e) {
    //             e.printStackTrace();
    //             return;
    //         }
    //         while (working) {
    //             try {
    //                 // modbus
    //                 // period1
    //                 direction = config.getD1();
    //                 velocity = config.getV1();
    //                 // motorRunner.exec(true, direction, velocity);
    //                 motor.changeVelocity((int) velocity);
    //                 motor.changeDirection(direction);
    //                 notify(true, direction, velocity);
    //                 Thread.sleep(config.getT1().longValue());
    //                 // period2
    //                 direction = config.getD2();
    //                 velocity = config.getV2();
    //                 // motorRunner.exec(true, direction, velocity);
    //                 motor.changeVelocity((int) velocity);
    //                 motor.changeDirection(direction);
    //                 notify(true, direction, velocity);
    //                 Thread.sleep(config.getT2().longValue());
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //         stop();
    //         notify(false, direction, 0f);
    //     });
    // }

    public void stop() {
        // motor.turnOff();
        try {
            sendHttp(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        working = false;
        if (motorDisposable != null && !motorDisposable.isDisposed()) {
            motorDisposable.dispose();
        }
    }

    private void sendHttp(boolean turnOn) throws Exception {
        System.out.println("GMT Driver: HTTP/Request [turn: " + turnOn + "]");
        List<PulseMotorHttpConfigItem> items = new ArrayList<>();
        items.add(new PulseMotorHttpConfigItem(config.getV1().intValue(), config.getT1().intValue(), config.getD1()));
        items.add(new PulseMotorHttpConfigItem(config.getV2().intValue(), config.getT2().intValue(), config.getD2()));
        PulseMotorHttpConfig payloadConfig = new PulseMotorHttpConfig(items, turnOn);
        PulseMotorHttpPayload payload = new PulseMotorHttpPayload(payloadConfig);
        Response response = OkHttpHelper.post("http://" + ip + "/api/v1/config", payload);
        if (response == null || !response.isSuccessful()) {
            // set success.
            throw new Exception("Http connect error.");
        }
    }

    private void notify(Boolean isOn, Boolean dir, Float velocity) {
        CoreActivity.getOkWebViewInstance().sendToWeb(new EventPayload(STREAM_NAME, 200, new PulseMotorNotify(tag, isOn, dir, velocity))).observeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<EventResponse>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                // Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "test", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(@NonNull EventResponse eventResponse) {
                // Log.i(TAG, eventResponse.toString());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }
        });
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
            this.isOn = false;
            this.d1 = false;
            this.v1 = 0f;
            this.t1 = 0f;
            this.d2 = false;
            this.v2 = 0f;
            this.t2 = 0f;
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

        public Boolean getIsOn() {
            return isOn;
        }

        public void setIsOn(Boolean on) {
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

    public static class PulseMotorNotify {
        private String tag;
        private Boolean isOn;
        private Boolean d;
        private Float v;

        public PulseMotorNotify() {
        }

        public PulseMotorNotify(String tag, Boolean isOn, Boolean d, Float v) {
            this.tag = tag;
            this.isOn = isOn;
            this.d = d;
            this.v = v;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Boolean getIsOn() {
            return isOn;
        }

        public void setIsOn(Boolean on) {
            isOn = on;
        }

        public Boolean getD() {
            return d;
        }

        public void setD(Boolean d) {
            this.d = d;
        }

        public Float getV() {
            return v;
        }

        public void setV(Float v) {
            this.v = v;
        }
    }

    public static class PulseMotorHttpPayload {
        private PulseMotorHttpConfig config;

        public PulseMotorHttpPayload(PulseMotorHttpConfig config) {
            this.config = config;
        }

        public PulseMotorHttpConfig getConfig() {
            return config;
        }

        public void setConfig(PulseMotorHttpConfig config) {
            this.config = config;
        }
    }

    public static class PulseMotorHttpConfig {
        private List<PulseMotorHttpConfigItem> items;
        private boolean turn;

        public PulseMotorHttpConfig(List<PulseMotorHttpConfigItem> items, boolean turn) {
            this.items = items;
            this.turn = turn;
        }

        public List<PulseMotorHttpConfigItem> getItems() {
            return items;
        }

        public void setItems(List<PulseMotorHttpConfigItem> items) {
            this.items = items;
        }

        public boolean getTurn() {
            return turn;
        }

        public void setTurn(boolean turn) {
            this.turn = turn;
        }
    }

    public static class PulseMotorHttpConfigItem {
        private int velocity;
        private int time;
        private boolean direction;

        public PulseMotorHttpConfigItem(int velocity, int time, boolean direction) {
            this.velocity = velocity;
            this.time = time;
            this.direction = direction;
        }

        public int getVelocity() {
            return velocity;
        }

        public void setVelocity(int velocity) {
            this.velocity = velocity;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public boolean getDirection() {
            return direction;
        }

        public void setDirection(boolean direction) {
            this.direction = direction;
        }
    }
}
