package io.okandroid.bluetooth.le;

import android.bluetooth.BluetoothGattCharacteristic;

public class OkBleCharacteristic {
    private BluetoothGattCharacteristic characteristic;
    private byte[] data;
    private long timestamp;

    public OkBleCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, long timestamp) {
        this.characteristic = characteristic;
        this.data = data;
        this.timestamp = timestamp;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
