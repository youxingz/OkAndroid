package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.core.Single;

/**
 * 通用信息
 */
public class GenericAccessService extends AbstractService {
    private static final UUID SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    private static final UUID APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    private static final UUID PPCP = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb"); // Peripheral Preferred Connection Parameters Characteristic
    private static final UUID CAR = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"); // Central Address Resolution Characteristic

    public GenericAccessService(OkBleClient client) {
        super("Generic Access Service", client);
    }


    /**
     * Device Name
     *
     * @return @String.
     */
    public Single<String> deviceName() {
        return readOnce(SERVICE, DEVICE_NAME, characteristic -> characteristic.getStringValue(0));
    }

    /**
     * Device Name
     *
     * @param name 设备更新名称
     * @return
     */
    public Single<String> deviceName(String name) {
        return writeOnce(SERVICE, DEVICE_NAME, name.getBytes(StandardCharsets.UTF_8), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, characteristic -> characteristic.getStringValue(0));
    }

    /**
     * Appearance
     *
     * @return @String, e.g. 960 (HID, Human Interface Device)
     */
    public Single<Integer> appearance() {
        return readOnce(SERVICE, APPEARANCE, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
    }


    /**
     * Peripheral Preferred Connection Parameters
     *
     * @return @String.
     */
    public Single<String> ppcp() {
        return readOnce(SERVICE, PPCP, characteristic -> characteristic.getStringValue(0));
    }

    /**
     * Central Address Resolution
     *
     * @return @String.
     */
    public Single<String> centralAddressResolution() {
        return readOnce(SERVICE, CAR, characteristic -> characteristic.getStringValue(0));
    }
}
