package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * 电池信息服务
 */
public class BatteryService extends AbstractService {
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final UUID BATTERY_LEVEL_DESC = UUID.fromString("00002908-0000-1000-8000-00805f9b34fb"); // 0x2908?

    public BatteryService(OkBleClient client) {
        super("Battery Service", client);
    }

    public Observable<Integer> batteryLevel() {
        // return readOnce(BATTERY_SERVICE, BATTERY_LEVEL, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        return observeNotification(BATTERY_SERVICE, BATTERY_LEVEL, BATTERY_LEVEL_DESC, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        // BluetoothGattCharacteristic characteristic_ = client.getCharacteristic(BATTERY_SERVICE, BATTERY_LEVEL);
        // client.enableNotification(characteristic_, BATTERY_LEVEL_DESC, true);
        // return readMulti(BATTERY_SERVICE, BATTERY_LEVEL, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
    }
}
