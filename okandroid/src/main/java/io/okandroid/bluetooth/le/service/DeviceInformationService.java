package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.OkBluetoothException;
import io.okandroid.bluetooth.le.OkBleCharacteristic;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备信息服务
 */
public class DeviceInformationService {
    public static final UUID DEVICE_INFORMATION_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");
    public static final UUID MODEL_NUMBER = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static final UUID MANUFACTURER_NAME = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID SERIAL_NUMBER = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");

    private Disposable deviceIdDisposable = null;
    private Disposable modelNumberDisposable = null;
    private Disposable manufacturerNameDisposable = null;
    private Disposable serialNumberDisposable = null;

    private OkBleClient client;

    public DeviceInformationService(OkBleClient client) {
        this.client = client;
    }


    /**
     * 设备ID
     *
     * @return hex array. e.g. 0x5544332211887766
     */
    public Single<byte[]> deviceId() {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSystemID = client.getCharacteristic(DEVICE_INFORMATION_SERVICE, SYSTEM_ID);
            if (characteristicSystemID == null) {
                emitter.onError(new OkBluetoothException("BLE device not support: [READ] System ID"));
                return;
            }
            deviceIdDisposable = client.readCharacteristic(characteristicSystemID).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(okBleCharacteristic -> {
                BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristic.getValue()); // hex byte array.
                    deviceIdDisposable.dispose();
                }
            });
        });
    }


    /**
     * Model Number String
     *
     * @return @String, e.g. A2084 (model name: AirPods Pro)
     */
    public Single<String> modelNumber() {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicModelNumber = client.getCharacteristic(DEVICE_INFORMATION_SERVICE, MODEL_NUMBER);
            if (characteristicModelNumber == null) {
                emitter.onError(new OkBluetoothException("BLE device not support: [READ] Model Number"));
                return;
            }
            modelNumberDisposable = client.readCharacteristic(characteristicModelNumber).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(okBleCharacteristic -> {
                BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristic.getStringValue(0));
                    modelNumberDisposable.dispose();
                }
            });
        });
    }


    /**
     * Manufacturer Name String
     *
     * @return @String, e.g. Apple Inc.
     */
    public Single<String> manufacturerName() {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicManufacturerName = client.getCharacteristic(DEVICE_INFORMATION_SERVICE, MANUFACTURER_NAME);
            if (characteristicManufacturerName == null) {
                emitter.onError(new OkBluetoothException("BLE device not support: [READ] Manufacturer Name"));
                return;
            }
            manufacturerNameDisposable = client.readCharacteristic(characteristicManufacturerName).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(okBleCharacteristic -> {
                BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristic.getStringValue(0));
                    manufacturerNameDisposable.dispose();

                }
            });
        });
    }

    /**
     * Serial Number, 序列号
     *
     * @return @String, e.g. H6VGFVZC1059
     */
    public Single<String> serialNumber() {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSerialNumber = client.getCharacteristic(DEVICE_INFORMATION_SERVICE, SERIAL_NUMBER);
            if (characteristicSerialNumber == null) {
                emitter.onError(new OkBluetoothException("BLE device not support: [READ] Serial Number"));
                return;
            }
            serialNumberDisposable = client.readCharacteristic(characteristicSerialNumber).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(okBleCharacteristic -> {
                BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristic.getStringValue(0));
                    serialNumberDisposable.dispose();

                }
            });
        });
    }
}
