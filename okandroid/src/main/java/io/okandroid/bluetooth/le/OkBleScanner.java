package io.okandroid.bluetooth.le;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.okandroid.bluetooth.OkBluetoothException;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * Permissions required:
 * <uses-permission android:name="android.permission.BLUETOOTH"/>
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
 * <p>
 * <!-- If your app targets Android 9 or lower, you can declare
 * ACCESS_COARSE_LOCATION instead. -->
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 */
public class OkBleScanner {
    private Context context;
    private BluetoothAdapter adapter;

    public OkBleScanner(Context context) {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Observable<ScanResult> scan() {
        return scan(new ScanSettings.Builder().build(), new ArrayList<>());
    }

    public Observable<ScanResult> scan(ScanSettings settings, List<ScanFilter> filters) {
        return Observable.create(new ObservableOnSubscribe<ScanResult>() {
            @SuppressLint("MissingPermission")
            @Override
            public void subscribe(@NonNull ObservableEmitter<ScanResult> emitter) {
                // if 5sec stuck, emitter.onComplete!
                final long[] lastFoundDeviceAt = {System.currentTimeMillis()};
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() > lastFoundDeviceAt[0] + 5000) {
                            if (!emitter.isDisposed()) {
                                emitter.onComplete();
                            }
                            this.cancel();
                        }
                    }
                }, 1000, 1000);
                try {
                    adapter.getBluetoothLeScanner().startScan(filters, settings, new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            super.onScanResult(callbackType, result);
                            if (emitter.isDisposed()) return;
                            // result.getScanRecord().getServiceUuids();
                            emitter.onNext(result);
                            lastFoundDeviceAt[0] = System.currentTimeMillis();
                        }

                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            super.onBatchScanResults(results);
                            if (emitter.isDisposed()) return;
                            emitter.onComplete();
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);
                            if (emitter.isDisposed()) return;
                            emitter.onError(new OkBluetoothException.ErrorCodeException(errorCode));
                        }
                    });
                } catch (SecurityException e) {
                    // Need android.permission.BLUETOOTH_SCAN permission
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        adapter.getBluetoothLeScanner().stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
    }
}
