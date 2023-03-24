package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 电池信息服务
 */
public class BatteryService extends AbstractService {
    public static final UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final UUID BATTERY_LEVEL_REPORT_DESC = UUID.fromString("00002908-0000-1000-8000-00805f9b34fb");

    public BatteryService(OkBleClient client) {
        super("Battery Service", client);
    }

    public Observable<Integer> batteryLevel() {
        // return readOnce(BATTERY_SERVICE, BATTERY_LEVEL, characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        return Observable.create(emitter -> {
            CharacteristicValueTaker<Integer> valueTaker = characteristic -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            observeNotification(BATTERY_SERVICE, BATTERY_LEVEL, CLIENT_CHARACTERISTIC_CONFIG_DESC, valueTaker)
                    .subscribeOn(OkAndroid.subscribeIOThread())
                    .observeOn(OkAndroid.newThread()) // NOTE! new thread instead of UI main thread.
                    .subscribe(new Observer<Integer>() {
                        private Disposable disposable;

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            this.disposable = d;
                            // read battery level
                            readOnce(BATTERY_SERVICE, BATTERY_LEVEL, valueTaker)
                                    .subscribeOn(OkAndroid.subscribeIOThread())
                                    .observeOn(OkAndroid.newThread()) // NOTE! new thread instead of UI main thread.
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {
                                        }

                                        @Override
                                        public void onSuccess(@NonNull Integer integer) {
                                            if (emitter != null && !emitter.isDisposed()) {
                                                emitter.onNext(integer);
                                            }
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            if (emitter != null && !emitter.isDisposed()) {
                                                emitter.onError(e);
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onNext(@NonNull Integer integer) {
                            if (emitter != null && !emitter.isDisposed()) {
                                emitter.onNext(integer);
                            } else {
                                disposable.dispose();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (emitter != null && !emitter.isDisposed()) {
                                emitter.onError(e);
                            } else {
                                disposable.dispose();
                            }
                        }

                        @Override
                        public void onComplete() {
                            if (emitter != null && !emitter.isDisposed()) {
                                emitter.onComplete();
                            } else {
                                disposable.dispose();
                            }
                        }
                    });
        });
    }
}
