package io.okandroid.bluetooth.le;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * - 修复兼容性问题：在部分机型上存在部分 callback 不存在的问题，统一封装以兼容各种版本设备
 * - 增加 characteristic 队列，以解决多处并发引起的丢包问题
 */
public abstract class OkBleGattCallback extends BluetoothGattCallback {
    private Queue<BluetoothGattCharacteristic> characteristics = new LinkedBlockingQueue<>();
    private BluetoothGatt gatt;

    private volatile boolean isRunning;

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public abstract void onOkBleCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value, int status);

    public abstract void onOkBleDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status, byte[] value);

    public abstract void onOkBleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value);

    @SuppressLint("MissingPermission")
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        characteristics.add(characteristic);
        continueReading();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        // if (status != BluetoothGatt.GATT_SUCCESS) return;
        isRunning = false;
        this.onOkBleCharacteristicRead(gatt, characteristic, characteristic.getValue(), status);
        continueReading();
    }

    @Override
    public void onCharacteristicRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value, int status) {
        super.onCharacteristicRead(gatt, characteristic, value, status);
        // if (status != BluetoothGatt.GATT_SUCCESS) return;
        isRunning = false;
        this.onOkBleCharacteristicRead(gatt, characteristic, value, status);
        continueReading();
    }

    @SuppressLint("MissingPermission")
    private void continueReading() {
        if (characteristics.isEmpty()) return;
        if (!isRunning) {
            isRunning = true;
            BluetoothGattCharacteristic characteristic = characteristics.poll();
            gatt.readCharacteristic(characteristic);
        }
    }

    @Deprecated
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        this.onOkBleDescriptorRead(gatt, descriptor, status, descriptor.getValue());
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status, byte[] value) {
        super.onDescriptorRead(gatt, descriptor, status, value);
        this.onOkBleDescriptorRead(gatt, descriptor, status, value);
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


}
