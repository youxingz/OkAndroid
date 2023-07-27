package com.cardioflex.bioreactor.motor;

import android.util.Log;

import com.cardioflex.bioreactor.CoreActivity;
import com.cardioflex.bioreactor.sys.Sys;
import com.cardioflex.bioreactor.x.EventPayload;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.okandroid.OkAndroid;
import io.okandroid.http.OkHttpHelper;
import io.okandroid.js.EventResponse;
import io.okandroid.sensor.motor.Leadshine57PumpQueued;
import io.okandroid.usb.USB;
import io.okandroid.utils.GsonUtils;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import kotlin.text.Charsets;
import okhttp3.Response;

public class PulseMotor {
    private static final String STREAM_NAME = "pulse-motor-stream";
    private static final HashMap<String, Integer> idStore = new HashMap<>();
    @Deprecated
    private static final String ip = "192.168.1.15";

    private static USB usb;
    private static CoreActivity activity;

    public static void setCoreActivityContext(CoreActivity activity) throws IOException {
        PulseMotor.activity = activity;
        PulseMotor.usb = new USB(activity);
        PulseMotor.usb.open(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
    }

    static {
        idStore.put("a1", 1);
        idStore.put("a2", 2);
        idStore.put("a3", 1);
        idStore.put("b1", 3);
        idStore.put("b2", 4);
        idStore.put("b3", 1);
    }

    private String tag; // a1, a2, a3, b1, b2, b3
    private Integer id;
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
        this.id = idStore.get(tag);
        if (id == null) {
            throw new IllegalArgumentException("Tag is invalid.");
        }
        this.motor = modbus;
        // 2023-07-27: add default = 0
        this.config = new PulseMotorConfig();
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

    // private void sendHttp(boolean turnOn) throws Exception {
    //     System.out.println("GMT Driver: HTTP/Request [turn: " + turnOn + "]");
    //     List<PulseMotorHttpConfigItem> items = new ArrayList<>();
    //     items.add(new PulseMotorHttpConfigItem(config.getV1().intValue(), config.getT1().intValue(), config.getD1()));
    //     items.add(new PulseMotorHttpConfigItem(config.getV2().intValue(), config.getT2().intValue(), config.getD2()));
    //     PulseMotorHttpConfig payloadConfig = new PulseMotorHttpConfig(items, turnOn);
    //     PulseMotorHttpPayload payload = new PulseMotorHttpPayload(payloadConfig, id);
    //     Response response = OkHttpHelper.post("http://" + ip + "/api/v1/config", payload);
    //     if (response == null || !response.isSuccessful()) {
    //         // set success.
    //         // throw new Exception("Http connect error.");
    //         Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "[GMT] 配置更新失败 / HTTP Error", BaseTransientBottomBar.LENGTH_LONG).show();
    //     } else {
    //         Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "[GMT] 配置更新成功", BaseTransientBottomBar.LENGTH_LONG).show();
    //     }
    //     try {
    //         if (response != null) response.body().close();
    //     } catch (Exception e) {
    //         throw new Exception("Http response body close error.");
    //     }
    // }
    private void sendHttp(boolean turnOn) throws Exception {
        System.out.println("GMT Driver: Serial Request");
        if (usb == null) return;
        System.out.println("GMT Driver: Serial Request [turn: " + turnOn + "]");
        List<PulseMotorHttpConfigItem> items = new ArrayList<>();
        items.add(new PulseMotorHttpConfigItem(config.getV1().intValue(), config.getT1().intValue(), config.getD1()));
        items.add(new PulseMotorHttpConfigItem(config.getV2().intValue(), config.getT2().intValue(), config.getD2()));
        PulseMotorHttpConfig payloadConfig = new PulseMotorHttpConfig(items, turnOn);
        PulseMotorHttpPayload payload = new PulseMotorHttpPayload(payloadConfig, id);
        String pay_str = GsonUtils.getInstance().toJson(payload).replaceAll("\n", "").replaceAll(" ", ""); // ⚠️：如果 payload 内容不可控，需要定制 Gson 解析器，而非使用直接替换
        System.out.println(pay_str);
        // to bytes
        byte[] pay_bytes = pay_str.getBytes(StandardCharsets.UTF_8);
        byte[] request_bytes = new byte[2 + pay_bytes.length];
        System.arraycopy(pay_bytes, 0, request_bytes, 2, pay_bytes.length);
        request_bytes[0] = 0x02;
        request_bytes[1] = 0x02;
        // sending...
        String approach = "REQUEST[" + payload.getReq() + "]: SUCCESS";
        usb.write(request_bytes);
        // loop read
        StringBuilder received = new StringBuilder();
        int timeout = 3000;
        boolean success = false;
        while (true) {
            if (timeout < 0) break;
            byte[] data = usb.read();
            received.append(new String(data, Charsets.UTF_8));
            System.out.print(received);
            if (received.toString().contains(approach)) {
                success = true;
                break;
            }
            timeout -= 200;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (success) {
            Log.i("GMT", "Update Config Success");
            Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "[GMT] 配置更新成功", BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            Log.i("GMT", "Update Config Failure");
            Snackbar.make(CoreActivity.getOkWebViewInstance().getWebView(), "[GMT] 配置更新失败 / USB Error", BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    // public static void main(String[] args) throws Exception {
    //     new PulseMotor("a1", null).sendHttp(true);
    // }

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
        private Boolean isOn = false;
        private Boolean d1 = false;
        private Float v1 = 0f;
        private Float t1 = 0f;
        private Boolean d2 = false;
        private Float v2 = 0f;
        private Float t2 = 0f;

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
        private Integer req;
        private PulseMotorHttpConfig config;
        private Integer motorId;

        public PulseMotorHttpPayload(PulseMotorHttpConfig config, Integer motorId) {
            this.req = (int) (Math.random() * Integer.MAX_VALUE);
            this.config = config;
            this.motorId = motorId;
        }

        public Integer getReq() {
            return req;
        }

        public void setReq(Integer req) {
            this.req = req;
        }

        public PulseMotorHttpConfig getConfig() {
            return config;
        }

        public void setConfig(PulseMotorHttpConfig config) {
            this.config = config;
        }

        public Integer getMotorId() {
            return motorId;
        }

        public void setMotorId(Integer motorId) {
            this.motorId = motorId;
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
