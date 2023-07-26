package com.cardioflex.bci;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.OkBleScanner;
import io.okandroid.bluetooth.le.service.BCIX16Service;
import io.okandroid.cardioflex.bci.ESP32C3;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Worker {
    FullscreenActivity activity;
    DeviceListAdapter deviceListAdapter;
    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(BCIX16Service.BCI_X16_SERVICE)).build(); // .setServiceUuid(new ParcelUuid(PulseGeneratorService.PULSE_GENERATOR_SERVICE)).build();

    OkBleScanner scanner;
    private ButtonMode mode = ButtonMode.start_scan;

    public Worker(FullscreenActivity activity, DeviceListAdapter deviceListAdapter) {
        this.activity = activity;
        this.deviceListAdapter = deviceListAdapter;
        this.scanner = new OkBleScanner(this.activity);
        // activity.contentTextView.setText("Hello!");
    }

    public void start() {
        switch (mode) {
            case start_scan: {
                // 开始扫描
                activity.contentTextView.setText("正在搜寻设备...");
                HashMap<String, BluetoothDevice> devices = new HashMap<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.checkPermission();
                }
                scanner.stopScan();
                scanner.scan(new ScanSettings.Builder().build(), Collections.singletonList(filter)).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread()).subscribe(new Observer<ScanResult>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        System.out.println("Start scan.");
                    }

                    @Override
                    public void onNext(@NonNull ScanResult scanResult) {
                        BluetoothDevice device = devices.get(scanResult.getDevice().getAddress());
                        if (device != null) {
                            return;
                        }
                        devices.put(scanResult.getDevice().getAddress(), scanResult.getDevice());
                        Worker.this.deviceListAdapter.addDevice(scanResult.getDevice());
                        Worker.this.deviceListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
                break;
            }
            case stop_sample: {
                stopSample(null);
                break;
            }
        }
    }

    private ESP32C3 esp32C3;
    private static String SECRET = "testsecret";

    Disposable disposableSample;

    public void startConnDevice(String macAddress) {
        updateContentText("开始连接...");
        esp32C3 = new ESP32C3(activity, macAddress);
        esp32C3.connect().subscribe(new Observer<OkBleClient.ConnectionStatus>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(OkBleClient.@NonNull ConnectionStatus connectionStatus) {
                if (connectionStatus == OkBleClient.ConnectionStatus.connected) {
                    updateContentText("设备已连接");
                    esp32C3.requestMtu(500);

                    // start listening.
                    if (disposableSample != null) {
                        if (!disposableSample.isDisposed()) {
                            disposableSample.dispose();
                        }
                        disposableSample = null;
                    }
                    // gen secret
                    SECRET = updateSecretText();
                    esp32C3.startSample(SECRET).subscribeOn(Schedulers.io()).observeOn(OkAndroid.newThread()).subscribe(new Observer<BCIX16Service.X16DataPayload>() {
                        private long lastReceivedAt;

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            packageCount = 0;
                            updateContentText("采样中...");
                            switchMode(ButtonMode.stop_sample);
                        }

                        @Override
                        public void onNext(BCIX16Service.@NonNull X16DataPayload x16DataPayload) {
                            if (x16DataPayload.secret == null) return; // ignore this.
                            // update ms:
                            if (x16DataPayload.data != null && !x16DataPayload.data.isEmpty()) {
                                long timestamp = x16DataPayload.data.get(0).timestamp;
                                long diff = (timestamp - lastReceivedAt) / 1000;
                                lastReceivedAt = timestamp;
                                updateDelayMsText(diff);
                                // save files:
                                
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                    return;
                }
                if (connectionStatus == OkBleClient.ConnectionStatus.disconnected) {
                    updateContentText("设备离线");
                    if (disposableSample != null) {
                        if (!disposableSample.isDisposed()) {
                            disposableSample.dispose();
                        }
                        disposableSample = null;
                    }
                    return;
                }
                updateContentText(connectionStatus.cname());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public void stopSample(String macAddress) {
        if (disposableSample != null) {
            if (!disposableSample.isDisposed()) {
                disposableSample.dispose();
            }
            disposableSample = null;
        }
        esp32C3.stopSample();
        esp32C3.disconnect();
    }

    public void updateContentText(String text) {
        OkAndroid.mainThread().scheduleDirect(() -> activity.contentTextView.setText(text));
    }

    private static long packageCount = 0;

    public void updateDelayMsText(long time) {
        OkAndroid.mainThread().scheduleDirect(() -> activity.delayMsTextView.setText("共收包：" + packageCount++ + "个，" + "延迟：" + time + " ms"));
    }

    public String updateSecretText() {
        String secret = UUID.randomUUID().toString();
        OkAndroid.mainThread().scheduleDirect(() -> activity.secretTextView.setText(secret));
        return secret;
    }

    public void switchMode(ButtonMode mode) {
        this.mode = mode;
        switch (mode) {
            case start_scan: {
                activity.actionButton.setText("搜索设备");
                break;
            }
            case start_sample: {
                activity.actionButton.setText("开始采样");
                break;
            }
            case stop_sample: {
                activity.actionButton.setText("停止采样");
                break;
            }
        }
    }

    public enum ButtonMode {
        start_scan, // start_conn,
        start_sample, stop_sample,
    }
}
