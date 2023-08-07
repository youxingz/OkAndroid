package io.okandroid.bluetooth.le;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothStatusCodes;
import android.os.Build;
import android.util.Log;

import java.util.Hashtable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import io.okandroid.bluetooth.OkBluetoothException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.SingleEmitter;

/**
 * - 修复兼容性问题：在部分机型上存在部分 callback 不存在的问题，统一封装以兼容各种版本设备
 * - 增加 characteristic 队列，以解决多处并发引起的丢包问题
 */
public abstract class OkBleGattCallback extends BluetoothGattCallback {
    private Queue<OkBleCharacteristicReadRequest> requestReadQueue = new LinkedBlockingQueue<>();
    private Queue<OkBleCharacteristicWriteRequest> requestWriteQueue = new LinkedBlockingQueue<>();

    private Hashtable<UUID, ObservableEmitter<OkBleCharacteristic>> characteristicChangeEmitterMap = new Hashtable<>();
    private BluetoothGatt gatt;

    private volatile boolean isReadRunning;
    private volatile boolean isWriteRunning;
    private SingleEmitter<OkBleCharacteristic> currentReadEmitter;
    private SingleEmitter<BluetoothGattCharacteristic> currentWriteEmitter;

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public abstract void onOkBleCharacteristicRead(BluetoothGatt gatt, SingleEmitter<OkBleCharacteristic> emitter, BluetoothGattCharacteristic characteristic, byte[] value, int status);

    public abstract void onOkBleCharacteristicWrite(BluetoothGatt gatt, SingleEmitter<BluetoothGattCharacteristic> emitter, BluetoothGattCharacteristic characteristic, int status);

    public abstract void onOkBleDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status, byte[] value);

    public abstract void onOkBleCharacteristicChanged(BluetoothGatt gatt, ObservableEmitter<OkBleCharacteristic> emitter, BluetoothGattCharacteristic characteristic, byte[] value);

    @SuppressLint("MissingPermission")
    public void readCharacteristic(OkBleCharacteristicReadRequest request) {
        requestReadQueue.add(request);
        continueReading();
    }

    @SuppressLint("MissingPermission")
    public void writeCharacteristic(OkBleCharacteristicWriteRequest request) {
        requestWriteQueue.add(request);
        continueWriting();
    }

    @SuppressLint("MissingPermission")
    public Observable<OkBleCharacteristic> observeNotification(BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor) {
        return Observable.create(emitter -> {
            Log.i("OKBLE", "try to observe notification");
            characteristicChangeEmitterMap.put(characteristic.getUuid(), emitter);
            boolean success = gatt.setCharacteristicNotification(characteristic, true);
            if (success) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    // gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE); // ? 带上就不一定会成功订阅
                    gatt.writeDescriptor(descriptor);
                }
                gatt.writeCharacteristic(characteristic);
            }
        });
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        // if (status != BluetoothGatt.GATT_SUCCESS) return;
        isReadRunning = false;
        SingleEmitter<OkBleCharacteristic> emitter = currentReadEmitter;
        continueReading();
        this.onOkBleCharacteristicRead(gatt, emitter, characteristic, characteristic.getValue(), status);
    }

    @Override
    public void onCharacteristicRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value, int status) {
        super.onCharacteristicRead(gatt, characteristic, value, status);
        // if (status != BluetoothGatt.GATT_SUCCESS) return;
        isReadRunning = false;
        SingleEmitter<OkBleCharacteristic> emitter = currentReadEmitter;
        continueReading();
        this.onOkBleCharacteristicRead(gatt, emitter, characteristic, value, status);
    }

    @SuppressLint("MissingPermission")
    private synchronized void continueReading() {
        if (requestReadQueue.isEmpty()) return;
        if (!isReadRunning) {
            isReadRunning = true;
            OkBleCharacteristicReadRequest request = requestReadQueue.poll();
            assert request != null;
            currentReadEmitter = request.getEmitter();
            gatt.readCharacteristic(request.getCharacteristic());
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        isWriteRunning = false;
        SingleEmitter<BluetoothGattCharacteristic> emitter = currentWriteEmitter;
        if (emitter == null) return;
        if (!emitter.isDisposed()) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                emitter.onSuccess(characteristic);
            } else {
                emitter.onError(new OkBluetoothException.DeviceWriteException(String.format("Device write error. status=[%d]", status), status));
            }
        }
        this.onOkBleCharacteristicWrite(gatt, emitter, characteristic, status);
        continueWriting();
    }


    @SuppressLint({"MissingPermission", "WrongConstant", "DefaultLocale"})
    private synchronized void continueWriting() {
        if (requestWriteQueue.isEmpty()) return;
        System.out.println(">> REST WRITE QUEUE: " + requestWriteQueue.size());
        if (!isWriteRunning) {
            isWriteRunning = true;
            OkBleCharacteristicWriteRequest request = requestWriteQueue.poll();
            assert request != null;
            SingleEmitter<BluetoothGattCharacteristic> emitter = request.getEmitter();
            BluetoothGattCharacteristic characteristic = request.getCharacteristic();
            currentWriteEmitter = emitter;
            int code = -1;
            while (request.retryTime-- > 0) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    code = gatt.writeCharacteristic(characteristic, request.getData(), request.getWriteType());
                } else {
                    characteristic.setValue(request.getData());
                    characteristic.setWriteType(request.getWriteType());
                    code = gatt.writeCharacteristic(characteristic) ? 0 : -1;
                }
                try {
                    Thread.sleep(10); // ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (code == 0) {
                    break;
                }
                System.out.println("RETRY...");
            }
            if (code != BluetoothStatusCodes.SUCCESS) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new OkBluetoothException.DeviceWriteException(String.format("Device write error. [%d]", code), code));
                }
            }
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
    @SuppressLint("MissingPermission")
    @Deprecated
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        ObservableEmitter<OkBleCharacteristic> emitter = characteristicChangeEmitterMap.get(characteristic.getUuid());
        if (emitter != null && !emitter.isDisposed()) {
            this.onOkBleCharacteristicChanged(gatt, emitter, characteristic, characteristic.getValue());
        } else {
            characteristicChangeEmitterMap.remove(characteristic.getUuid());
            gatt.setCharacteristicNotification(characteristic, false); // disable notification.
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        super.onCharacteristicChanged(gatt, characteristic, value);
        ObservableEmitter<OkBleCharacteristic> emitter = characteristicChangeEmitterMap.get(characteristic.getUuid());
        if (emitter != null && !emitter.isDisposed()) {
            this.onOkBleCharacteristicChanged(gatt, emitter, characteristic, value);
        } else {
            characteristicChangeEmitterMap.remove(characteristic.getUuid());
            gatt.setCharacteristicNotification(characteristic, false); // disable notification.
        }
    }


    public static class OkBleCharacteristicReadRequest {
        private SingleEmitter<OkBleCharacteristic> emitter;
        private BluetoothGattCharacteristic characteristic;

        public OkBleCharacteristicReadRequest(SingleEmitter<OkBleCharacteristic> emitter, BluetoothGattCharacteristic characteristic) {
            this.emitter = emitter;
            this.characteristic = characteristic;
        }

        public SingleEmitter<OkBleCharacteristic> getEmitter() {
            return emitter;
        }

        public BluetoothGattCharacteristic getCharacteristic() {
            return characteristic;
        }
    }

    public static class OkBleCharacteristicWriteRequest {
        public int retryTime = 3;
        private SingleEmitter<BluetoothGattCharacteristic> emitter;
        private BluetoothGattCharacteristic characteristic;

        private byte[] data;
        private int writeType;

        public OkBleCharacteristicWriteRequest(SingleEmitter<BluetoothGattCharacteristic> emitter, BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
            this.emitter = emitter;
            this.characteristic = characteristic;
            this.data = data;
            this.writeType = writeType;
        }

        public SingleEmitter<BluetoothGattCharacteristic> getEmitter() {
            return emitter;
        }

        public BluetoothGattCharacteristic getCharacteristic() {
            return characteristic;
        }

        public byte[] getData() {
            return data;
        }

        public int getWriteType() {
            return writeType;
        }
    }
}
