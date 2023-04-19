package io.okandroid.bluetooth.le.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.OkBluetoothException;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;

public class PulseGeneratorService extends AbstractService {
    public static final UUID PULSE_GENERATOR_SERVICE = UUID.fromString("0000face-0000-1000-8000-00805f9b34fb");
    public static final UUID PULSE_WAVE_CHAR = UUID.fromString("0000fac1-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化
    public static final UUID PULSE_PARAMS_CHAR = UUID.fromString("0000fac2-0000-1000-8000-00805f9b34fb"); // 发生波形
    public static final UUID PULSE_WAVE_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化 desc


    public PulseGeneratorService(OkBleClient client) {
        super("Pulse Generator Service", client);
    }

    public Observable<int[]> currentWave() {
        return observeNotification(PULSE_GENERATOR_SERVICE, PULSE_WAVE_CHAR, PULSE_WAVE_DESC, new CharacteristicValueTaker<int[]>() {
            @Override
            public int[] takeValue(BluetoothGattCharacteristic characteristic) {
                byte[] resp = characteristic.getValue();
                // to int
                int[] data = new int[resp.length / 2];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (resp[2 * i] << 8) | (resp[2 * i + 1] & 0xFF);
                }
                return data;
            }
        });
    }

    public Single<List<BluetoothGattCharacteristic>> sendWave(List<WaveParam> params) {
        return Single.create(emitter -> {
            BluetoothGattCharacteristic characteristic = client.getCharacteristic(PULSE_GENERATOR_SERVICE, PULSE_PARAMS_CHAR);
            if (characteristic == null) {
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onError(new OkBluetoothException(String.format("BLE device not support service: %s / %s", PULSE_GENERATOR_SERVICE, PULSE_PARAMS_CHAR)));
                }
                return;
            }
            // 并发（实际上会被串行）
            List<SingleSource<BluetoothGattCharacteristic>> singles = new ArrayList<>();
            int total = params.size();
            for (int i = 0; i < total; i++) {
                WaveParam param = params.get(i);
                byte[] data = param.toPayload(total, i);
                Single<BluetoothGattCharacteristic> single = client.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                //.onErrorResumeNext(new Function<Throwable, SingleSource<? extends BluetoothGattCharacteristic>>() {
                //     @Override
                //     public SingleSource<? extends BluetoothGattCharacteristic> apply(Throwable throwable) throws Throwable {
                //         return new SingleSource<BluetoothGattCharacteristic>() {
                //             @Override
                //             public void subscribe(@NonNull SingleObserver<? super BluetoothGattCharacteristic> observer) {
                //                 // observer.onSuccess();
                //             }
                //         };
                //     }
                // });
                singles.add(single);
            }

            Single.merge(singles)
                    .observeOn(OkAndroid.mainThread())
                    .subscribeOn(OkAndroid.subscribeIOThread())
                    .subscribe(new FlowableSubscriber<BluetoothGattCharacteristic>() {
                        List<BluetoothGattCharacteristic> results; //= new LinkedList<>();

                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSubscribe(Subscription s) {
                            // start.
                            // client.getBluetoothGatt().beginReliableWrite();
                            results = new ArrayList<>();
                            s.request(singles.size());
                        }

                        @Override
                        public void onNext(BluetoothGattCharacteristic characteristic) {
                            results.add(characteristic);
                        }

                        @SuppressLint("MissingPermission")
                        @Override
                        public void onError(Throwable t) {
                            // client.getBluetoothGatt().executeReliableWrite();
                            if (emitter != null && !emitter.isDisposed()) {
                                emitter.onError(t);
                            }
                        }

                        @SuppressLint("MissingPermission")
                        @Override
                        public void onComplete() {
                            // client.getBluetoothGatt().executeReliableWrite();
                            if (emitter != null && !emitter.isDisposed()) {
                                emitter.onSuccess(results);
                            }
                        }
                    });
        });
    }

    public static class WaveParam {
        private Type type;
        private int length;
        private int[] params; // 16-bits

        public enum Type {
            square(1), exp(2), pulse(3), sin(4), cos(5), constant(6);
            int code;

            Type(int code) {
                this.code = code;
            }

            public int getCode() {
                return this.code;
            }
        }

        public WaveParam(Type type, int length, int[] params) {
            this.type = type;
            this.length = length;
            this.params = params;
            if (params.length < length) throw new RuntimeException("Params length is not correct.");
        }

        public WaveParam() {
        }

        public byte[] toPayload(int total, int index) {
            byte[] data = new byte[length * 2 + 6];
            int id = type.getCode();
            data[0] = (byte) (id >> 8 & 0xFF);
            data[1] = (byte) (id & 0xFF);
            data[2] = (byte) (total >> 8 & 0xFF);
            data[3] = (byte) (total & 0xFF);
            data[4] = (byte) (index >> 8 & 0xFF);
            data[5] = (byte) (index & 0xFF);
            // params:
            for (int i = 0; i < length; i++) {
                int param = params[i];
                data[6 + 2 * i] = (byte) (param >> 8 & 0xFF);
                data[6 + 2 * i + 1] = (byte) (param & 0xFF);
            }
            return data;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int[] getParams() {
            return params;
        }

        public void setParams(int[] params) {
            this.params = params;
        }
    }
}
