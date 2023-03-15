package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Hashtable;
import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.OkBluetoothException;
import io.okandroid.bluetooth.le.OkBleCharacteristic;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class AbstractService {
    protected OkBleClient client;
    protected String serviceName;

    private Hashtable<UUID, Disposable> disposes = new Hashtable<>();

    public AbstractService(String serviceName, OkBleClient client) {
        this.serviceName = serviceName;
        this.client = client;
    }


    protected <T> Single<T> writeOnce(UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType, CharacteristicValueTaker<T> characteristicValueTaker) {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSystemID = client.getCharacteristic(serviceUUID, characteristicUUID);
            if (characteristicSystemID == null) {
                emitter.onError(new OkBluetoothException(String.format("BLE device not support: [READ] %s / %s", serviceName, characteristicUUID)));
                return;
            }
            Disposable thisDispose = client.writeCharacteristic(characteristicSystemID, data, writeType).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(characteristic -> {
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristicValueTaker.takeValue(characteristic)); // hex byte array.
                    Disposable disposable = disposes.get(characteristicUUID);
                    if (disposable != null) {
                        disposable.dispose();
                        disposes.remove(characteristicUUID);
                    }
                }
            });
            disposes.put(characteristicUUID, thisDispose);
        });
    }

    protected <T> Single<T> readOnce(UUID serviceUUID, UUID characteristicUUID, CharacteristicValueTaker<T> characteristicValueTaker) {
        return Single.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSystemID = client.getCharacteristic(serviceUUID, characteristicUUID);
            if (characteristicSystemID == null) {
                emitter.onError(new OkBluetoothException(String.format("BLE device not support: [READ] %s / %s", serviceName, characteristicUUID)));
                return;
            }
            Disposable thisDispose = client.readCharacteristic(characteristicSystemID).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(okBleCharacteristic -> {
                BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                if (characteristic == null) return;
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onSuccess(characteristicValueTaker.takeValue(characteristic)); // hex byte array.
                    Disposable disposable = disposes.get(characteristicUUID);
                    if (disposable != null) {
                        disposable.dispose();
                        disposes.remove(characteristicUUID);
                    }
                }
            });
            disposes.put(characteristicUUID, thisDispose);
        });
    }

    protected <T> Observable<T> readMulti(UUID serviceUUID, UUID characteristicUUID, CharacteristicValueTaker<T> characteristicValueTaker) {
        return Observable.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSystemID = client.getCharacteristic(serviceUUID, characteristicUUID);
            if (characteristicSystemID == null) {
                emitter.onError(new OkBluetoothException(String.format("BLE device not support: [READ] %s / %s", serviceName, characteristicUUID)));
                return;
            }
            client.readCharacteristic(characteristicSystemID).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<OkBleCharacteristic>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull OkBleCharacteristic okBleCharacteristic) {
                    BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                    if (characteristic == null) return;
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onNext(characteristicValueTaker.takeValue(characteristic)); // hex byte array.
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }

                @Override
                public void onComplete() {
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onComplete();
                    }
                }
            });
        });
    }


    protected <T> Observable<T> observeNotification(UUID serviceUUID, UUID characteristicUUID, CharacteristicValueTaker<T> characteristicValueTaker) {
        return Observable.create(emitter -> {
            if (!client.isConnected()) {
                emitter.onError(new OkBluetoothException("BLE device is not connected."));
                return;
            }
            BluetoothGattCharacteristic characteristicSystemID = client.getCharacteristic(serviceUUID, characteristicUUID);
            if (characteristicSystemID == null) {
                emitter.onError(new OkBluetoothException(String.format("BLE device not support: [NOTIFY] %s / %s", serviceName, characteristicUUID)));
                return;
            }
            client.observeNotification(characteristicSystemID).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<OkBleCharacteristic>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onNext(@NonNull OkBleCharacteristic okBleCharacteristic) {
                    BluetoothGattCharacteristic characteristic = okBleCharacteristic.getCharacteristic();
                    if (characteristic == null) return;
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onNext(characteristicValueTaker.takeValue(characteristic)); // hex byte array.
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }

                @Override
                public void onComplete() {
                    if (emitter != null && !emitter.isDisposed()) {
                        emitter.onComplete();
                    }
                }
            });
        });
    }

    protected interface CharacteristicValueTaker<T> {
        T takeValue(BluetoothGattCharacteristic characteristic);
    }
}
