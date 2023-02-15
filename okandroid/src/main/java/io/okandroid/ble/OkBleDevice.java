package io.okandroid.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothStatusCodes;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.UUID;

import io.okandroid.bluetooth.OkBluetoothMessage;
import io.okandroid.exception.OkBluetoothException;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class OkBleDevice {

    private Context context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    //    private ConnectionStatus connectionStatus;
    private BluetoothGattCallback gattCallback;
    private BluetoothDevice device;

    private ObservableEmitter<ConnectionStatus> connectionEmitter;
    private ObservableEmitter<OkBluetoothMessage> readCharacteristicEmitter;
    private SingleEmitter<Integer> readMtuEmitter;

    public enum ConnectionStatus {
        connecting,
        connected,
        disconnected,
        disconnecting,
        //        connecting_lost,
        //        fail,
    }

    public OkBleDevice(Context context, BluetoothDevice device) {
        this.context = context;
        this.device = device;
    }

    private boolean initialize(@NonNull ObservableEmitter<ConnectionStatus> emitter) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                //                Log.e(TAG, "Unable to initialize BluetoothManager.");
                emitter.onError(new OkBluetoothException("Unable to initialize BluetoothManager."));
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            //            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            emitter.onError(new OkBluetoothException("Unable to obtain a BluetoothAdapter."));
            return false;
        }

        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                // gatt.getServices().get(0).getUuid();
                if (gatt.getServices() != null)
                    for (BluetoothGattService service : gatt.getServices()) {
                        UUID uuid = service.getUuid();
                        System.out.println(uuid);
                    }
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (connectionEmitter != null && !connectionEmitter.isDisposed()) {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            connectionEmitter.onNext(ConnectionStatus.connected);
                            break;
                        case BluetoothProfile.STATE_CONNECTING:
                            connectionEmitter.onNext(ConnectionStatus.connecting);
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            connectionEmitter.onNext(ConnectionStatus.disconnected);
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            connectionEmitter.onNext(ConnectionStatus.disconnecting);
                            break;
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value, int status) {
                super.onCharacteristicRead(gatt, characteristic, value, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (readCharacteristicEmitter != null && !readCharacteristicEmitter.isDisposed()) {
                    readCharacteristicEmitter.onNext(new OkBluetoothMessage(gatt.getDevice(), value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
            }

            @Override
            public void onDescriptorRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattDescriptor descriptor, int status, @androidx.annotation.NonNull byte[] value) {
                super.onDescriptorRead(gatt, descriptor, status, value);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                if (readMtuEmitter != null && !readMtuEmitter.isDisposed()) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        readMtuEmitter.onSuccess(mtu);
                    }
                }
            }

            @Override
            public void onServiceChanged(@androidx.annotation.NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);
            }
        };
        return true;
    }

    @SuppressLint("MissingPermission")
    public Observable<ConnectionStatus> connect(boolean autoConnect) {
        return Observable.create(new ObservableOnSubscribe<ConnectionStatus>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<ConnectionStatus> emitter) throws Throwable {
                connectionEmitter = emitter;
                initialize(emitter);
                String address = device.getAddress();
                if (mBluetoothAdapter == null || address == null) {
                    //                    Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                    //                    return false;
                    emitter.onError(new OkBluetoothException("BluetoothAdapter not initialized or unspecified address."));
                    return;
                }

                // Previously connected device.  Try to reconnect.
                if (mBluetoothGatt != null) {
                    // Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                    if (mBluetoothGatt.connect()) {
                        // connectionStatus = ConnectionStatus.connecting;
                        emitter.onNext(ConnectionStatus.connecting);
                    }
                }

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                if (device == null) {
                    //                    Log.w(TAG, "Device not found.  Unable to connect.");
                    emitter.onError(new OkBluetoothException.DeviceNotFoundException("Device not found. Unable to connect."));
                    return;
                }
                if (gattCallback == null) {
                    emitter.onError(new OkBluetoothException("GattCallback not set. Please try to re-connect this device."));
                    return;
                }
                mBluetoothGatt = device.connectGatt(context, autoConnect, gattCallback);
                emitter.onNext(ConnectionStatus.connecting);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            //            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (connectionEmitter != null && !connectionEmitter.isDisposed()) {
            connectionEmitter.onNext(ConnectionStatus.disconnected);
            connectionEmitter.onComplete();
        }
        mBluetoothGatt.disconnect();
        close();
    }

    @SuppressLint("MissingPermission")
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @SuppressLint("MissingPermission")
    public void setMtu(int mtu) {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.requestMtu(mtu);
    }

    @SuppressLint("MissingPermission")
    public @NonNull Flowable<Integer> writeMtu(int mtu) {
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
            //            setMtu(mtu);
            if (mBluetoothGatt == null) {
                emitter.onError(new OkBluetoothException("Can not write MTU. BluetoothGatt is null"));
                return;
            }
            mBluetoothGatt.requestMtu(mtu);
            emitter.onSuccess(mtu);
        }).concatWith(readMtu());
    }

    public Single<Integer> readMtu() {
        return Single.create(emitter -> readMtuEmitter = emitter);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    public Single<Integer> writeCharacteristic(String uuid, byte[] data, int writeType) {
        return Single.create(emitter -> {
            if (mBluetoothGatt == null) {
                emitter.onError(new OkBluetoothException("Bluetooth Gatt is null."));
                return;
            }
            List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();
            BluetoothGattCharacteristic characteristic = null;
            for (BluetoothGattService service : serviceList) {
                characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if (characteristic != null) {
                    break;
                }
            }
            if (characteristic == null) {
                emitter.onError(new OkBluetoothException("No characteristic match."));
                return;
            }
            int code = mBluetoothGatt.writeCharacteristic(characteristic, data, writeType);
            if (emitter.isDisposed()) return;
            switch (code) {
                case BluetoothStatusCodes.SUCCESS:
                    emitter.onSuccess(code);
                    break;
                default:
                    emitter.onError(new OkBluetoothException.DeviceWriteException("Device write error.", code));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    public Single<Integer> writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        return Single.create(emitter -> {
            int code = mBluetoothGatt.writeCharacteristic(characteristic, data, writeType);
            if (emitter.isDisposed()) return;
            switch (code) {
                case BluetoothStatusCodes.SUCCESS:
                    emitter.onSuccess(code);
                    break;
                default:
                    emitter.onError(new OkBluetoothException.DeviceWriteException("Device write error.", code));
            }
        });
    }

    public Observable<OkBluetoothMessage> readCharacteristic() {
        return Observable.create(emitter -> readCharacteristicEmitter = emitter);
    }
}
