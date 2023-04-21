package com.cardioflex.pulse_generator;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;

import com.cardioflex.pulse_generator.x.EventPayload;

import java.util.Collections;
import java.util.HashMap;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleScanner;
import io.okandroid.bluetooth.le.service.PulseGeneratorService;
import io.okandroid.cardioflex.pulsegen.Nordic52832;
import io.okandroid.js.EventResponse;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PageDeviceList {
    private final static String TAG = "PageDeviceListAct";
    private static CoreActivity coreActivity;

    public static void setCoreActivity(CoreActivity coreActivity) {
        PageDeviceList.coreActivity = coreActivity;
    }

    private static volatile boolean scanDone = true;

    @SuppressLint("CheckResult")
    public static void startScanDevice() {
        if (!scanDone) {
            return; // do nothing if it was still working.
        }
        OkBleScanner scanner = new OkBleScanner(coreActivity);
        // ScanFilter filter = new ScanFilter.Builder().build(); //.setServiceUuid(new ParcelUuid(PulseGeneratorService.PULSE_GENERATOR_SERVICE)).build();
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(PulseGeneratorService.PULSE_GENERATOR_SERVICE)).build();
        scanner.scan(new ScanSettings.Builder().build(), Collections.singletonList(filter)).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread()).subscribe(new Observer<ScanResult>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                scanDone = false;
            }

            @Override
            public void onNext(@NonNull ScanResult scanResult) {
                Log.i(TAG, "find one...");
                // System.out.println(scanResult.getScanRecord().getDeviceName());
                // String mac = scanResult.getDevice().getAddress();
                // System.out.println(mac);
                HashMap<String, Object> data = new HashMap<>();
                HashMap<String, String> device = new HashMap<>();
                device.put("name", scanResult.getScanRecord().getDeviceName());
                device.put("mac", scanResult.getDevice().getAddress());
                data.put("device", device);
                data.put("done", scanDone);
                CoreActivity.getOkWebViewInstance().sendToWeb(new EventPayload("ble_device", 0, "", data)).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<EventResponse>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull EventResponse eventResponse) {
                        System.out.println(eventResponse);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(@NonNull Throwable e) {
                scanDone = true;
            }

            @Override
            public void onComplete() {
                scanDone = true;
            }
        });
    }

}
