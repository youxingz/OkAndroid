package com.cardioflex.bioreactor.opc.control_config;

import android.util.Log;

import com.cardioflex.bioreactor.CoreActivity;
import com.cardioflex.bioreactor.x.EventPayload;

import io.okandroid.OkAndroid;
import io.okandroid.exception.OkOPCException;
import io.okandroid.js.EventResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class LiquidMotor {

    private static final String STREAM_NAME = "liquid-motor-stream-";
    private LiquidMotor1 liquidMotor1;
    private LiquidMotor2 liquidMotor2;
    // 当前配置
    private LiquidMotor1.EquitPump1Payload pump1Payload;
    private LiquidMotor2.EquitPump2Payload pump2Payload;

    private volatile boolean working = false;

    private static LiquidMotor instance;

    private LiquidMotor() {
    }

    public static LiquidMotor getInstance() {
        if (instance == null) {
            instance = new LiquidMotor();
        }
        return instance;
    }

    public void setLiquidMotor1(LiquidMotor1 liquidMotor1_, LiquidMotor1.EquitPump1Payload pump1Payload_) {
        this.liquidMotor1 = liquidMotor1_;
        this.pump1Payload = new LiquidMotor1.EquitPump1Payload(pump1Payload_.getIsOn(), pump1Payload_.getD(), pump1Payload_.getV(), pump1Payload_.getT());
    }

    public void setLiquidMotor2(LiquidMotor2 liquidMotor2_, LiquidMotor2.EquitPump2Payload pump2Payload_) {
        this.liquidMotor2 = liquidMotor2_;
        this.pump2Payload = new LiquidMotor2.EquitPump2Payload(pump2Payload_.getIsOn(), pump2Payload_.getD(), pump2Payload_.getV(), pump2Payload_.getT());
    }

    public LiquidMotor1.EquitPump1Payload getPump1Payload() {
        return pump1Payload;
    }

    public LiquidMotor2.EquitPump2Payload getPump2Payload() {
        return pump2Payload;
    }

    /**
     * 需要 LiquidMotor1,2 两者都设置后才可启动
     */
    public void start() {
        if (liquidMotor1 == null || liquidMotor2 == null) {
            return;
        }
        OkAndroid.newThread().scheduleDirect(() -> {
            // stop it
            stop();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            working = true;
            while (working) {
                try {
                    // pump1
                    pump1Payload.setIsOn(true);
                    notify(1, pump1Payload);
                    liquidMotor1.writeConfig(pump1Payload);

                    // wait
                    Thread.sleep(pump1Payload.getT().longValue());

                    pump1Payload.setIsOn(false);
                    notify(1, pump1Payload);
                    liquidMotor1.writeConfig(pump1Payload);

                    // pump2
                    pump2Payload.setIsOn(true);
                    notify(2, pump2Payload);
                    liquidMotor2.writeConfig(pump2Payload);

                    // wait
                    Thread.sleep(pump2Payload.getT().longValue());

                    liquidMotor2.writeConfig(pump2Payload);
                    notify(2, pump2Payload);
                    pump2Payload.setIsOn(false);
                } catch (OkOPCException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        working = false;
    }

    private void notify(int index, Object data) {
        CoreActivity.getOkWebViewInstance().sendToWeb(new EventPayload(STREAM_NAME + index, 200, data)).observeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<EventResponse>() {
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
}
