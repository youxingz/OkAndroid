package io.okandroid.bluetooth.le.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.OkBluetoothException;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;

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
                /**
                 * 除去前 6 byte, 剩余内容为：time: 4byte, volt: 2byte
                 */
                byte[] resp = characteristic.getValue();
                // System.out.println(Arrays.toString(resp));
                int psize = (Byte.toUnsignedInt(resp[4]) << 8 & 0xFF) | (Byte.toUnsignedInt(resp[5]) & 0xFF);
                psize /= 3;
                if (6 + psize * 6 > resp.length) {
                    // error
                    return new int[0];
                }
                // System.out.println(resp.length);
                // assert (resp.length - 6) % 3 == 0;
                // to int
                int[] data = new int[3 + psize * 2];
                data[0] = Byte.toUnsignedInt(resp[0]) << 8 | (Byte.toUnsignedInt(resp[1]) & 0xFF); // id
                data[1] = Byte.toUnsignedInt(resp[2]) << 8 | (Byte.toUnsignedInt(resp[3]) & 0xFF); // index
                data[2] = psize; // package data size
                int tmp;
                for (int i = 0; i < psize; i += 1) {
                    // time
                    tmp = (Byte.toUnsignedInt(resp[6 * i + 6]) << 24);
                    tmp |= (Byte.toUnsignedInt(resp[6 * i + 7]) << 16);
                    tmp |= (Byte.toUnsignedInt(resp[6 * i + 8]) << 8);
                    tmp |= (Byte.toUnsignedInt(resp[6 * i + 9]) & 0xFF);
                    data[2 * i + 3] = tmp;
                    // volt
                    tmp = (Byte.toUnsignedInt(resp[6 * i + 10]) << 8);
                    tmp |= (Byte.toUnsignedInt(resp[6 * i + 11]) & 0xFF);
                    data[2 * i + 4] = tmp - (1 << 15);
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
                byte[] data = param.toPayload((int) (System.currentTimeMillis() / 1000), total, i);
                System.out.println(Arrays.toString(data));
                if (data == null)
                    continue;
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

        public byte[] toPayload(int id, int total, int index) {
            byte[] data = null;
            // params:
            if (type == Type.square) {
                data = new byte[length * 2 + 10];
                int command = type.getCode();
                data[0] = (byte) (id >> 8 & 0xFF);
                data[1] = (byte) (id & 0xFF);
                data[2] = (byte) (total >> 8 & 0xFF);
                data[3] = (byte) (total & 0xFF);
                data[4] = (byte) (index >> 8 & 0xFF);
                data[5] = (byte) (index & 0xFF);
                data[6] = (byte) (command >> 8 & 0xFF);
                data[7] = (byte) (command & 0xFF);
                if (params.length < 5) {
                    // error.
                    return null;
                }
                int volt = params[0];
                int first_positive = params[1];
                int count = params[2];
                int is_alternate = params[3];
                int period_us = params[4];
                int duty_100 = params[5];
                data[8] = (byte) (volt >> 8 & 0xFF);
                data[9] = (byte) (volt & 0xFF);
                data[10] = (byte) (count >> 8 & 0xFF);
                data[11] = (byte) (count & 0xFF);
                data[12] = (byte) (period_us >> 24 & 0xFF);
                data[13] = (byte) (period_us >> 16 & 0xFF);
                data[14] = (byte) (period_us >> 8 & 0xFF);
                data[15] = (byte) (period_us & 0xFF);
                data[16] = (byte) (duty_100 >> 8 & 0xFF);
                data[17] = (byte) (duty_100 & 0xFF);
                data[18] = (byte) (first_positive & 0xFF);
                data[19] = (byte) (is_alternate & 0xFF);
            } else {
                data = new byte[length * 2 + 8];
                int command = type.getCode();
                data[0] = (byte) (id >> 8 & 0xFF);
                data[1] = (byte) (id & 0xFF);
                data[2] = (byte) (total >> 8 & 0xFF);
                data[3] = (byte) (total & 0xFF);
                data[4] = (byte) (index >> 8 & 0xFF);
                data[5] = (byte) (index & 0xFF);
                data[6] = (byte) (command >> 8 & 0xFF);
                data[7] = (byte) (command & 0xFF);
                for (int i = 0; i < length; i++) {
                    int param = params[i];
                    data[8 + 2 * i] = (byte) (param >> 8 & 0xFF);
                    data[8 + 2 * i + 1] = (byte) (param & 0xFF);
                }
            }
            return data;
        }

        public int timeNeed() {
            int time = 0;
            switch (type) {
                case square: {
                    time = params[2] * params[4]; // count * period_us
                    break;
                }
                case pulse: {
                    time = params[1]; // period_us
                    break;
                }
                // TODO! 支持更多类型的波形时需要在这里计算最终耗时
            }
            return time;
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
