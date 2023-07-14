package io.okandroid.bluetooth.le;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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

/**
 * 低功耗蓝牙 Client
 */
public class OkBleClient {

    private final Context context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private OkBleGattCallback gattCallback;
    private final String macAddress;

    // emitters
    private ObservableEmitter<ConnectionStatus> connectionEmitter;
    private ObservableEmitter<OkBleDescriptor> readDescriptorEmitter;
    private ObservableEmitter<BluetoothGattDescriptor> writeDescriptorEmitter;
    private ObservableEmitter<List<BluetoothGattService>> serviceDiscoverEmitter;
    private ObservableEmitter<List<BluetoothGattService>> serviceChangeEmitter;
    private SingleEmitter<Integer> readMtuEmitter;
    private SingleEmitter<Integer> readRssiEmitter;

    public enum ConnectionStatus {
        connecting, services_discovering, connected, disconnected, disconnecting,
    }

    private static ConnectionStatus connectionStatus = ConnectionStatus.disconnected;

    public OkBleClient(Context context, String macAddress) {
        this.context = context;
        this.macAddress = macAddress;
    }

    public OkBleClient(Context context, ScanResult deviceScanResult) {
        this(context, deviceScanResult.getDevice().getAddress());
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

        gattCallback = new OkBleGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                // gatt.getServices().get(0).getUuid();
                // if (gatt.getServices() != null)
                //     for (BluetoothGattService service : gatt.getServices()) {
                //         UUID uuid = service.getUuid();
                //         System.out.println(uuid);
                //     }
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                mBluetoothGatt = gatt;
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        connectionStatus = ConnectionStatus.services_discovering;
                        mBluetoothGatt.discoverServices(); // 连接后自动扫描可用服务
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        connectionStatus = ConnectionStatus.connecting;
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        connectionStatus = ConnectionStatus.disconnected;
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        connectionStatus = ConnectionStatus.disconnecting;
                        break;
                }
                if (connectionEmitter != null && !connectionEmitter.isDisposed()) {
                    connectionEmitter.onNext(connectionStatus);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                connectionStatus = ConnectionStatus.connected;
                if (connectionEmitter != null && !connectionEmitter.isDisposed()) {
                    connectionEmitter.onNext(connectionStatus);
                }
                if (serviceDiscoverEmitter != null && !serviceDiscoverEmitter.isDisposed()) {
                    serviceDiscoverEmitter.onNext(gatt.getServices());
                }
            }

            @Override
            public void onOkBleCharacteristicRead(BluetoothGatt gatt, SingleEmitter<OkBleCharacteristic> emitter, BluetoothGattCharacteristic characteristic, byte[] value, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(new OkBleCharacteristic(characteristic, value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onOkBleCharacteristicWrite(BluetoothGatt gatt, SingleEmitter<BluetoothGattCharacteristic> emitter, BluetoothGattCharacteristic characteristic, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                if (emitter == null) return;
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(characteristic);
                }
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) return;
                // TODO! after: gatt.executeReliableWrite()
            }

            /**
             * notify
             * @param characteristic
             */
            @Override
            public void onOkBleCharacteristicChanged(@androidx.annotation.NonNull BluetoothGatt gatt, ObservableEmitter<OkBleCharacteristic> emitter, @androidx.annotation.NonNull BluetoothGattCharacteristic characteristic, @androidx.annotation.NonNull byte[] value) {
                // super.onCharacteristicChanged(gatt, characteristic, value);
                // find target
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onNext(new OkBleCharacteristic(characteristic, value, System.currentTimeMillis()));
                }
            }

            @Override
            public void onOkBleDescriptorRead(@androidx.annotation.NonNull BluetoothGatt gatt, @androidx.annotation.NonNull BluetoothGattDescriptor descriptor, int status, @androidx.annotation.NonNull byte[] value) {
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
            String address = macAddress;
            if (mBluetoothAdapter == null || address == null) {
                emitter.onError(new OkBluetoothException("BluetoothAdapter not initialized or unspecified address."));
                return;
            }

            if (mBluetoothGatt != null) {
                if (mBluetoothGatt.connect()) {
                    connectionStatus = ConnectionStatus.connecting;
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
            gattCallback.setGatt(mBluetoothGatt);
            connectionStatus = ConnectionStatus.connecting;
            emitter.onNext(ConnectionStatus.connecting);
        });
    }

    public boolean isConnected() {
        return connectionStatus == ConnectionStatus.connected;
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

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
        if (mBluetoothGatt == null) throw new IllegalAccessError("Please connect to device first.");
        // for (BluetoothGattService service : mBluetoothGatt.getServices()) {
        //     System.out.println(service.getUuid());
        // }
        BluetoothGattService service = mBluetoothGatt.getService(serviceUuid);
        if (service == null)
            throw new IllegalArgumentException("Unknown Service UUID: " + serviceUuid);
        // BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        // if (characteristic == null)
        //     throw new IllegalArgumentException("Unknown Characteristic UUID: " + characteristicUuid);
        return service.getCharacteristic(characteristicUuid);
    }

    @SuppressLint("MissingPermission")
    public boolean enableNotification(BluetoothGattCharacteristic characteristic, UUID descriptorUUID, boolean enabled) {
        if (mBluetoothGatt == null) throw new IllegalAccessError("Please connect to device first.");
        boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (!success) return false;
        // write descriptor:
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            success = mBluetoothGatt.writeDescriptor(descriptor);
        }
        return success;
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

    public Single<BluetoothGattCharacteristic> writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, int writeType) {
        return writeCharacteristic(characteristic, null, null, data, writeType);
    }

    public Single<BluetoothGattCharacteristic> writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType) {
        return writeCharacteristic(null, serviceUUID, characteristicUUID, data, writeType);
    }

    // @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private Single<BluetoothGattCharacteristic> writeCharacteristic(final BluetoothGattCharacteristic characteristic, UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType) {
        return Single.create(emitter -> {
            if (!validGatt(emitter)) return;
            BluetoothGattCharacteristic characteristic_ = characteristic;
            if (characteristic_ == null) {
                characteristic_ = this.getCharacteristic(serviceUUID, characteristicUUID);
                if (characteristic_ == null) {
                    emitter.onError(new OkBluetoothException(String.format("BLE device not support: [WRITE / Characteristic] %s / %s", serviceUUID, characteristicUUID)));
                    return;
                }
            }
            gattCallback.writeCharacteristic(new OkBleGattCallback.OkBleCharacteristicWriteRequest(emitter, characteristic_, data, writeType));
            // int code = -1;
            // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            //     code = mBluetoothGatt.writeCharacteristic(characteristic_, data, writeType);
            // } else {
            //     characteristic_.setValue(data);
            //     characteristic_.setWriteType(writeType);
            //     code = mBluetoothGatt.writeCharacteristic(characteristic_) ? 0 : -1;
            // }
            // writeCharacteristicEmitterMap.put(characteristic_, emitter);
            // if (emitter.isDisposed()) return;
            // if (code != BluetoothStatusCodes.SUCCESS) {
            //     emitter.onError(new OkBluetoothException.DeviceWriteException("Device write error.", code));
            // }
        });
    }

    public Single<OkBleCharacteristic> readCharacteristic(BluetoothGattCharacteristic characteristic) {
        return readCharacteristic(characteristic, null, null);
    }

    @SuppressLint("MissingPermission")
    public Single<OkBleCharacteristic> readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        return readCharacteristic(null, serviceUUID, characteristicUUID);
    }

    @SuppressLint("MissingPermission")
    private Single<OkBleCharacteristic> readCharacteristic(final BluetoothGattCharacteristic characteristic, UUID serviceUUID, UUID characteristicUUID) {
        return Single.create(emitter -> {
            if (!validGatt(emitter)) return;
            BluetoothGattCharacteristic characteristic_ = characteristic;
            if (characteristic_ == null) {
                characteristic_ = this.getCharacteristic(serviceUUID, characteristicUUID);
                if (characteristic_ == null) {
                    emitter.onError(new OkBluetoothException(String.format("BLE device not support: [READ / Characteristic] %s / %s", serviceUUID, characteristicUUID)));
                    return;
                }
            }
            // mBluetoothGatt.readCharacteristic(characteristic);
            gattCallback.readCharacteristic(new OkBleGattCallback.OkBleCharacteristicReadRequest(emitter, characteristic_)); // stack 串行
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

    @SuppressLint("MissingPermission")
    public Observable<OkBleCharacteristic> observeNotification(BluetoothGattDescriptor descriptor) {
        return gattCallback.observeNotification(descriptor);
        // return Observable.create(emitter -> {
        //     if (!validGatt(emitter)) return;
        //     BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        //     boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        //     if (success) {
        //         characteristicChangeEmitterMap.put(characteristic, emitter);
        //         // BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(characteristic.getUuid()));
        //         descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //         descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        //         success = mBluetoothGatt.writeDescriptor(descriptor);
        //     }
        // });
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


    @SuppressLint("MissingPermission")
    public void simpleWrite(UUID serviceUUID, UUID characteristicUUID, byte[] data) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID, characteristicUUID);
        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @SuppressLint("MissingPermission")
    public Observable simpleSubscribe(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID, characteristicUUID);
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return Observable.create(emitter -> {

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
