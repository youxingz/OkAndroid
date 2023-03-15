package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.core.Observable;

/**
 * 电池信息服务
 */
public class BatteryService extends AbstractService {
    private static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public BatteryService(OkBleClient client) {
        super("Battery Service", client);
    }

    public Observable<Integer> batteryLevel() {
        return observeNotification(BATTERY_SERVICE, BATTERY_LEVEL, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
    }
}
