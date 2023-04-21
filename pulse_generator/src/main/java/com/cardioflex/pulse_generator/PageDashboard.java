package com.cardioflex.pulse_generator;

import android.annotation.SuppressLint;
import android.util.Log;

import com.cardioflex.pulse_generator.x.EventPayload;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.cardioflex.pulsegen.Nordic52832;
import io.okandroid.js.EventResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class PageDashboard {
    private final static String TAG = "PageDashboardAct";
    private static Nordic52832 nordic52832;
    private static OkBleClient.ConnectionStatus nrf52832ConnectionStatus;
    private static CoreActivity coreActivity;

    public static void setCoreActivity(CoreActivity coreActivity) {
        PageDashboard.coreActivity = coreActivity;
    }

    public static Nordic52832 getNordic52832Instance() {
        return nordic52832;
    }

    public static OkBleClient.ConnectionStatus getNrf52832ConnectionStatus() {
        return nrf52832ConnectionStatus;
    }

    @SuppressLint("CheckResult")
    public static void startConnect(String macAddress) {
        OkAndroid.newThread().scheduleDirect(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 58:61:C0:60:94:99
            nordic52832 = new Nordic52832(coreActivity, macAddress); // "F5:34:F4:78:DB:AA"
            nordic52832.connect().subscribe(new Observer<OkBleClient.ConnectionStatus>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(OkBleClient.@NonNull ConnectionStatus connectionStatus) {
                    Log.i(TAG, "connection::" + connectionStatus.name());
                    if (connectionStatus == OkBleClient.ConnectionStatus.connected) {
                        // do something.
                    }
                    nrf52832ConnectionStatus = connectionStatus;
                    CoreActivity.getOkWebViewInstance().sendToWeb(new EventPayload("ble_status", connectionStatus.ordinal(), connectionStatus.name(), null)).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<EventResponse>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull EventResponse eventResponse) {
                            System.out.println(eventResponse);
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {

                }
            });
        });
    }
}
