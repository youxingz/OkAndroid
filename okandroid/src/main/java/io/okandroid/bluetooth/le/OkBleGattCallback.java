package io.okandroid.bluetooth.le;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class OkBleGattCallback extends BluetoothGattCallback {
    private Queue<BluetoothGattCharacteristic> characteristics = new LinkedBlockingQueue<>();
    private BluetoothGatt gatt;

    private volatile boolean isRunning;

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    @SuppressLint("MissingPermission")
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        characteristics.add(characteristic);
        continueReading();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status != BluetoothGatt.GATT_SUCCESS) return;
        isRunning = false;
        continueReading();
    }

    @Override
    public void onCharacteristicRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value, int status) {
        super.onCharacteristicRead(gatt, characteristic, value, status);
        if (status != BluetoothGatt.GATT_SUCCESS) return;
        isRunning = false;
        continueReading();
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        this.onOkBleCharacteristicChanged(gatt, characteristic, characteristic.getValue());
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        super.onCharacteristicChanged(gatt, characteristic, value);
        this.onOkBleCharacteristicChanged(gatt, characteristic, value);
    }

    public abstract void onOkBleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value);

    @SuppressLint("MissingPermission")
    private void continueReading() {
        if (characteristics.isEmpty()) return;
        if (!isRunning) {
            isRunning = true;
            BluetoothGattCharacteristic characteristic = characteristics.poll();
            gatt.readCharacteristic(characteristic);
        }
    }

}
