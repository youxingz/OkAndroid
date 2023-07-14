package io.okandroid.bluetooth.le.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;

import org.reactivestreams.Subscription;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.OkBluetoothException;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BCIX16Service extends AbstractService {
    public static final UUID BCI_X16_SERVICE = UUID.fromString("0000fade-0000-1000-8000-00805f9b34fb");
    public static final UUID CMD_CHAR = UUID.fromString("0000fad1-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化
    public static final UUID CMD_RESULT_CHAR = UUID.fromString("0000fad2-0000-1000-8000-00805f9b34fb"); // 发生波形
    public static final UUID SAMPLE_CHAR = UUID.fromString("0000fad3-0000-1000-8000-00805f9b34fb"); // 发生波形
    public static final UUID PULSE_WAVE_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化 desc


    public BCIX16Service(OkBleClient client) {
        super("Cardioflex BCI X16 Service", client);
    }

    public Observable<int[]> startSample(String secret) {
        startLoopSyncTimestamp(secret);
        return observeNotification(BCI_X16_SERVICE, SAMPLE_CHAR, PULSE_WAVE_DESC, new CharacteristicValueTaker<int[]>() {
            @Override
            public int[] takeValue(BluetoothGattCharacteristic characteristic) {
                /**
                 * 除去前 6 byte, 剩余内容为：time: 4byte, volt: 2byte
                 */
                byte[] resp = characteristic.getValue();
                System.out.println(Arrays.toString(resp));
                return null;
            }
        });
    }

    private Disposable syncDisposable;

    private void startLoopSyncTimestamp(String secret) {
        // 使用 0x04 指令即可
        if (syncDisposable != null) {
            if (!syncDisposable.isDisposed()) {
                syncDisposable.dispose();
            }
            syncDisposable = null;
        }
        syncDisposable = OkAndroid.newThread().schedulePeriodicallyDirect(() -> {
            byte[] payload = ("{\"cmd\":4, \"secret\": \"" + secret + "\",\"ts\":" + System.currentTimeMillis() + "}").getBytes(StandardCharsets.UTF_8);
            client.simpleWrite(BCI_X16_SERVICE, CMD_CHAR, payload);
            // writeOnce(BCI_X16_SERVICE, CMD_CHAR, payload, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, v -> new String(v.getValue(), StandardCharsets.UTF_8)).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new SingleObserver<String>() {
            //     @Override
            //     public void onSubscribe(@NonNull Disposable d) {
            //         System.out.println("========================>>>");
            //         System.out.println(new Date().toString());
            //     }
            //
            //     @Override
            //     public void onSuccess(@NonNull String s) {
            //         System.out.println("========================<<<");
            //         System.out.println(new Date().toString());
            //     }
            //
            //     @Override
            //     public void onError(@NonNull Throwable e) {
            //
            //     }
            // });
        }, 1, 1, TimeUnit.SECONDS);
    }
}
