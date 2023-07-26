package io.okandroid.bluetooth.le.service;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class BCIX16Service extends AbstractService {
    public static final UUID BCI_X16_SERVICE = UUID.fromString("0000fade-0000-1000-8000-00805f9b34fb");
    public static final UUID CMD_CHAR = UUID.fromString("0000fad1-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化
    public static final UUID CMD_RESULT_CHAR = UUID.fromString("0000fad2-0000-1000-8000-00805f9b34fb"); // 发生波形
    public static final UUID SAMPLE_CHAR = UUID.fromString("0000fad3-0000-1000-8000-00805f9b34fb"); // 发生波形
    public static final UUID PULSE_WAVE_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 订阅通知：波形回传变化 desc
    private X16DataPayloadParser payloadParser = new X16DataPayloadParser();
    private volatile boolean working = false;
    private volatile boolean sampling = false;

    public BCIX16Service(OkBleClient client) {
        super("Cardioflex BCI X16 Service", client);
    }

    public Observable<BCIX16Service.X16DataPayload> startSample(String secret) {
        startLoopSyncTimestamp(secret);
        return observeNotification(BCI_X16_SERVICE, SAMPLE_CHAR, PULSE_WAVE_DESC, characteristic -> payloadParser.parse(secret, characteristic.getValue()));
    }

    public void stopSample() {
        sampling = false;
    }

    private Disposable syncDisposable;

    private void startLoopSyncTimestamp(String secret) {
        sampling = true;
        if (working) {
            return;
        }
        working = true;
        // 使用 0x04 指令即可
        // if (syncDisposable != null) {
        //     if (!syncDisposable.isDisposed()) {
        //         syncDisposable.dispose();
        //     }
        //     syncDisposable = null;
        // }
        syncDisposable = OkAndroid.newThread().scheduleDirect(() -> {
            while (sampling) {
                byte[] payload = ("{\"cmd\":4, \"secret\": \"" + secret + "\",\"ts\":" + System.currentTimeMillis() + "}").getBytes(StandardCharsets.UTF_8);
                System.out.println(":::::::::::::::::::" + new Date().toString());
                // client.simpleWrite(BCI_X16_SERVICE, CMD_CHAR, payload);
                client.writeCharacteristic(BCI_X16_SERVICE, CMD_CHAR, payload, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).subscribe();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public static class X16DataPayloadParser {

        public BCIX16Service.X16DataPayload parse(String secret, byte[] data) {
            int index = 0;
            X16DataPayload payload = new X16DataPayload();
            // starts with: 0x73, 0x02
            if (data[0] != 0x02) return payload;
            try {
                List<X16DataPayloadPoint> dataPoints = new ArrayList<>();
                payload.data = dataPoints;
                // parse.
                index = 1;
                long timestamp = 0;
                long timestamp_pre = 0;
                long timestamp_suf = 0;
                int size = 0;
                int channel_value = 0;
                int channel_index = 0;
                Integer[] acPoints = null;
                Integer[] dcPoints = null;
                // max size = 5
                int P_SIZE = 5;
                while (index < data.length) {
                    if (--P_SIZE < 0) break;
                    // timestamp: 8 bytes
                    timestamp_pre = (long) Byte.toUnsignedInt(data[index++]) << 24;
                    timestamp_pre |= (long) Byte.toUnsignedInt(data[index++]) << 16;
                    timestamp_pre |= (long) Byte.toUnsignedInt(data[index++]) << 8;
                    timestamp_pre |= (long) Byte.toUnsignedInt(data[index++]);
                    timestamp_suf = (long) Byte.toUnsignedInt(data[index++]) << 24;
                    timestamp_suf |= (long) Byte.toUnsignedInt(data[index++]) << 16;
                    timestamp_suf |= (long) Byte.toUnsignedInt(data[index++]) << 8;
                    timestamp_suf |= Byte.toUnsignedInt(data[index++]);
                    timestamp = (timestamp_pre << 32) | timestamp_suf;
                    size = Byte.toUnsignedInt(data[index++]) << 8;
                    size |= Byte.toUnsignedInt(data[index++]);
                    if (size != 16) return payload; // !important
                    acPoints = new Integer[size];
                    dcPoints = new Integer[size];
                    for (channel_index = 0; channel_index < size; channel_index++) {
                        // AC Amp. A[*]
                        channel_value = Byte.toUnsignedInt(data[index++]) << 8;
                        channel_value |= Byte.toUnsignedInt(data[index++]);
                        acPoints[channel_index] = channel_value;
                        // DC Amp. W[*]
                        channel_value = Byte.toUnsignedInt(data[index++]) << 8;
                        channel_value |= Byte.toUnsignedInt(data[index++]);
                        dcPoints[channel_index] = channel_value;
                    }
                    X16DataPayloadPoint point = new X16DataPayloadPoint();
                    point.acPoints = (acPoints);
                    point.dcPoints = (dcPoints);
                    point.timestamp = (timestamp);
                    dataPoints.add(point);
                }
                payload.secret = secret;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return payload;
        }
    }

    public static class X16DataPayload {
        public String secret;
        public List<X16DataPayloadPoint> data;
    }

    public static class X16DataPayloadPoint {
        public Long timestamp;
        public Integer[] acPoints;
        public Integer[] dcPoints;
    }

}
