package com.cardioflex.pulse_generator.x;

import android.bluetooth.BluetoothGattCharacteristic;

import com.cardioflex.pulse_generator.CoreActivity;
import com.cardioflex.pulse_generator.PageDashboard;
import com.cardioflex.pulse_generator.PageDeviceList;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.util.List;

import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.service.PulseGeneratorService;
import io.okandroid.utils.GsonUtils;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@RestController
public class XController {

    private static volatile boolean waveSending = false;
    private static volatile long lastSendWaveAt = System.currentTimeMillis();

    @GetMapping("/local-api/test")
    public String test(@RequestParam("text") String text) {
        return "Resp: " + text;
    }

    @PostMapping("/local-api/trigger")
    public String triggerWaveSend(@RequestBody String payloadStr, HttpResponse response) throws InterruptedException {
        if (System.currentTimeMillis() > lastSendWaveAt + 10000) { // 10s
            waveSending = false;
            lastSendWaveAt = System.currentTimeMillis();
        }
        if (waveSending) {
            response.setStatus(403);
            response.setBody(new StringBody("Wave Sending... [Denied]"));
            return "Wave Sending... [Denied]";
        }
        List<PulseGeneratorService.WaveParam> params = GsonUtils.fromJsonList(payloadStr, PulseGeneratorService.WaveParam.class);
        if (params.size() == 0) {
            response.setStatus(400);
            response.setBody(new StringBody("Invalid Size. [0]"));
            return "Invalid Size. [0]";
        }
        waveSending = true;
        if (PageDashboard.getNrf52832ConnectionStatus() == OkBleClient.ConnectionStatus.connected) {
            final String[] respText = {"success"};
            PageDashboard.getNordic52832Instance().sendWave(params).blockingSubscribe(new SingleObserver<List<BluetoothGattCharacteristic>>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    waveSending = true;
                }

                @Override
                public void onSuccess(@NonNull List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
                    waveSending = false;
                    if (bluetoothGattCharacteristics.size() == params.size()) {
                        int totalTimeRequired = params.stream().map(PulseGeneratorService.WaveParam::timeNeed).reduce(0, Integer::sum);
                        respText[0] = "" + totalTimeRequired;
                        response.setStatus(201);
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    waveSending = false;
                    e.printStackTrace();
                    respText[0] = "error";
                    response.setStatus(400);
                }
            });
            while (waveSending) {
                Thread.sleep(10);
            }
            return respText[0];
        } else {
            waveSending = false;
            response.setStatus(400);
            response.setBody(new StringBody("Device is not connected."));
            return "Device is not connected.";
        }
    }


    @PostMapping("/local-api/search-device-start")
    public String startScan(HttpResponse response) {
        PageDeviceList.startScanDevice();
        response.setStatus(201);
        return "扫描中...";
    }
}
