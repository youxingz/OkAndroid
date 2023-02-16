package io.okandroid.bluetooth.le;

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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import java.util.List;
import java.util.UUID;

import io.okandroid.bluetooth.OkBluetoothException;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class OkBleClient {

    private Context context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private BluetoothGattCallback gattCallback;
    private ScanResult deviceScanResult;

    // emitters
    private ObservableEmitter<ConnectionStatus> connectionEmitter;
    private ObservableEmitter<OkBleCharacteristic> readCharacteristicEmitter;
    private ObservableEmitter<BluetoothGattCharacteristic> writeCharacteristicEmitter;
    private ObservableEmitter<OkBleDescriptor> readDescriptorEmitter;
    private ObservableEmitter<BluetoothGattDescriptor> writeDescriptorEmitter;
    private ObservableEmitter<List<BluetoothGattService>> serviceDiscoverEmitter;
    private ObservableEmitter<List<BluetoothGattService>> serviceChangeEmitter;
    private ObservableEmitter<OkBleCharacteristic> characteristicChangeEmitter;
    private SingleEmitter<Integer> readMtuEmitter;
    private SingleEmitter<Integer> readRssiEmitter;

    public enum ConnectionStatus {
        connecting, connected, disconnected, disconnecting,
    }

    public OkBleClient(Context context, ScanResult deviceScanResult) {
        this.context = context;
        this.deviceScanResult = deviceScanResult;
    }

    private boolean connection_initialize(@NonNull ObservableEmitter<ConnectionStatus> emitter) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                emitter.onError(new OkBluetoothException("Unable to initialize BluetoothManager."));
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
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
                mBluetoothGatt = gatt;
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
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (serviceDiscoverEmitter != null && !serviceDiscoverEmitter.isDisposed()) {
                    serviceDiscoverEmitter.onNext(gatt.getServices());
                }
            }

            @Override
            public void onCharacteristicRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value, int status) {
                super.onCharacteristicRead(gatt, characteristic, value, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (readCharacteristicEmitter != null && !readCharacteristicEmitter.isDisposed()) {
                    readCharacteristicEmitter.onNext(new OkBleCharacteristic(characteristic, value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (writeCharacteristicEmitter != null && !writeCharacteristicEmitter.isDisposed()) {
                    writeCharacteristicEmitter.onNext(characteristic);
                }
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (writeCharacteristicEmitter != null && !writeCharacteristicEmitter.isDisposed()) {
                    writeCharacteristicEmitter.onComplete();
                }
            }

            @Override
            public void onCharacteristicChanged(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
                if (characteristicChangeEmitter != null && !characteristicChangeEmitter.isDisposed()) {
                    characteristicChangeEmitter.onNext(new OkBleCharacteristic(characteristic, value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onDescriptorRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattDescriptor descriptor, int status, @androidx.annotation.NonNull byte[] value) {
                super.onDescriptorRead(gatt, descriptor, status, value);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (readDescriptorEmitter != null && !readDescriptorEmitter.isDisposed()) {
                    readDescriptorEmitter.onNext(new OkBleDescriptor(descriptor, value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (writeDescriptorEmitter != null && !writeDescriptorEmitter.isDisposed()) {
                    writeDescriptorEmitter.onNext(descriptor);
                }
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (readRssiEmitter != null && !readRssiEmitter.isDisposed()) {
                    readRssiEmitter.onSuccess(rssi);
                    readRssiEmitter = null;
                }
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (readMtuEmitter != null && !readMtuEmitter.isDisposed()) {
                    readMtuEmitter.onSuccess(mtu);
                    readMtuEmitter = null;
                }
            }

            @Override
            public void onServiceChanged(@androidx.annotation.NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);
                if (serviceChangeEmitter != null && !serviceChangeEmitter.isDisposed()) {
                    serviceChangeEmitter.onNext(gatt.getServices());
                }
            }
        };
        return true;
    }

    @SuppressLint("MissingPermission")
    public Observable<ConnectionStatus> connect(boolean autoConnect) {
        return Observable.create(emitter -> {
            connectionEmitter = emitter;
            if (!connection_initialize(emitter)) return;
            String address = deviceScanResult.getDevice().getAddress();
            if (mBluetoothAdapter == null || address == null) {
                emitter.onError(new OkBluetoothException("BluetoothAdapter not initialized or unspecified address."));
                return;
            }

            if (mBluetoothGatt != null) {
                if (mBluetoothGatt.connect()) {
                    emitter.onNext(ConnectionStatus.connecting);
                }
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                emitter.onError(new OkBluetoothException.DeviceNotFoundException("Device not found. Unable to connect."));
                return;
            }
            if (gattCallback == null) {
                emitter.onError(new OkBluetoothException("GattCallback not set. Please try to re-connect this device."));
                return;
            }
            mBluetoothGatt = device.connectGatt(context, autoConnect, gattCallback);
            emitter.onNext(ConnectionStatus.connecting);
        });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (connectionEmitter != null && !connectionEmitter.isDisposed()) {
            connectionEmitter.onNext(ConnectionStatus.disconnected);
            connectionEmitter.onComplete();
        }
        mBluetoothGatt.disconnect();
        // close();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    // common api
    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
        if (mBluetoothGatt == null) throw new IllegalAccessError("Please connect to device first.");
        BluetoothGattService service = mBluetoothGatt.getService(serviceUuid);
        if (service == null)
            throw new IllegalArgumentException("Unknown Service UUID: " + serviceUuid);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        if (characteristic == null)
            throw new IllegalArgumentException("Unknown Characteristic UUID: " + characteristicUuid);
        return characteristic;
    }

    @SuppressLint("MissingPermission")
    public @NonNull Single<Integer> setMtu(int mtu) {
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
            if (!validGatt(emitter)) return;
            mBluetoothGatt.requestMtu(mtu);
            readMtuEmitter = emitter;
            // emitter.onSuccess(mtu);
        });
    }

    @SuppressLint("MissingPermission")
    public @NonNull Single<Integer> readRemoteRssi() {
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
            if (!validGatt(emitter)) return;
            mBluetoothGatt.readRemoteRssi();
            readMtuEmitter = emitter;
        });
    }

    // @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    public Observable<BluetoothGattCharacteristic> writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        return Observable.create(emitter -> {
            if (!validGatt(emitter)) return;
            int code = -1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                code = mBluetoothGatt.writeCharacteristic(characteristic, data, writeType);
            } else {
                characteristic.setValue(data);
                characteristic.setWriteType(writeType);
                code = mBluetoothGatt.writeCharacteristic(characteristic) ? 0 : -1;
            }
            writeCharacteristicEmitter = emitter;
            if (emitter.isDisposed()) return;
            if (code != BluetoothStatusCodes.SUCCESS) {
                emitter.onError(new OkBluetoothException.DeviceWriteException("Device write error.", code));
            }
        });
    }

    @SuppressLint("MissingPermission")
    public Observable<OkBleCharacteristic> readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return Observable.create(emitter -> {
            if (!validGatt(emitter)) return;
            mBluetoothGatt.readCharacteristic(characteristic);
            readCharacteristicEmitter = emitter;
        });
    }

    @SuppressLint("MissingPermission")
    public Observable<BluetoothGattDescriptor> writeDescriptor(BluetoothGattDescriptor descriptor, byte[] value) {
        return Observable.create(emitter -> {
            if (!validGatt(emitter)) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBluetoothGatt.writeDescriptor(descriptor, value);
            } else {
                descriptor.setValue(value); // ?
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            writeDescriptorEmitter = emitter;
        });
    }

    @SuppressLint("MissingPermission")
    public Observable<OkBleDescriptor> readDescriptor(BluetoothGattDescriptor descriptor) {
        return Observable.create(emitter -> {
            if (!validGatt(emitter)) return;
            mBluetoothGatt.readDescriptor(descriptor);
            readDescriptorEmitter = emitter;
        });
    }

    public Observable<OkBleCharacteristic> observeCharacteristicChanging() {
        return Observable.create(emitter -> {
            characteristicChangeEmitter = emitter;
        });
    }

    public Observable<List<BluetoothGattService>> observeServiceChanging() {
        return Observable.create(emitter -> {
            serviceChangeEmitter = emitter;
        });
    }

    @SuppressLint("MissingPermission")
    public Observable<List<BluetoothGattService>> discoverServices() {
        return Observable.create(emitter -> {
            if (!validGatt(emitter)) return;
            mBluetoothGatt.discoverServices();
            serviceDiscoverEmitter = emitter;
        });
    }

    // private

    private boolean validGatt(ObservableEmitter emitter) {
        if (mBluetoothGatt == null) {
            emitter.onError(new OkBluetoothException("Bluetooth Gatt is null. Please connect this device first."));
            return false;
        }
        return true;
    }

    private boolean validGatt(SingleEmitter emitter) {
        if (mBluetoothGatt == null) {
            emitter.onError(new OkBluetoothException("Bluetooth Gatt is null. Please connect this device first."));
            return false;
        }
        return true;
    }
}
