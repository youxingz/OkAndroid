package io.okandroid.bluetooth;

import android.bluetooth.BluetoothDevice;

public class OkBluetoothMessage {
    private BluetoothDevice device; // from or to
    private byte[] data;
    private long timestamp;

    public OkBluetoothMessage(BluetoothDevice device, byte[] data, long timestamp) {
        this.device = device;
        this.data = data;
        this.timestamp = timestamp;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
