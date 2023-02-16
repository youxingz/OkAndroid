package io.okandroid.bluetooth.le;

import android.bluetooth.BluetoothGattDescriptor;

public class OkBleDescriptor {
    private BluetoothGattDescriptor descriptor;
    private byte[] data;
    private long timestamp;

    public OkBleDescriptor(BluetoothGattDescriptor descriptor, byte[] data, long timestamp) {
        this.descriptor = descriptor;
        this.data = data;
        this.timestamp = timestamp;
    }
}
