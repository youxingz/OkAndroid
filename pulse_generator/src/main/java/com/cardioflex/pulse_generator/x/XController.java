package com.cardioflex.pulse_generator.x;

import android.bluetooth.BluetoothGattCharacteristic;

import com.cardioflex.pulse_generator.DashboardActivity;
import com.google.gson.internal.LinkedTreeMap;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.util.List;
import java.util.stream.Collectors;

import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.service.PulseGeneratorService;
import io.okandroid.utils.GsonUtils;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@RestController
public class XController {

    private static volatile boolean waveSending = false;

    @GetMapping("/local-api/test")
    public String test(@RequestParam("text") String text) {
        return "Resp: " + text;
    }

    @PostMapping("/local-api/trigger")
    public String triggerWaveSend(@RequestBody String payloadStr, HttpResponse response) throws InterruptedException {
        if (waveSending) {
            response.setStatus(403);
            response.setBody(new StringBody("Wave Sending... [Denied]"));
            return "Wave Sending... [Denied]";
        }
        waveSending = true;
        List<PulseGeneratorService.WaveParam> params = GsonUtils.fromJsonList(payloadStr, PulseGeneratorService.WaveParam.class);
        if (DashboardActivity.getNrf52832ConnectionStatus() == OkBleClient.ConnectionStatus.connected) {
            final String[] respText = {"success"};
            DashboardActivity.getNordic52832Instance().sendWave(params).blockingSubscribe(new SingleObserver<List<BluetoothGattCharacteristic>>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    waveSending = true;
                }

                @Override
                public void onSuccess(@NonNull List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
                    waveSending = false;
                    if (bluetoothGattCharacteristics.size() == params.size()) {
                        respText[0] = "success";
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

    public static class WaveParamPayload {
        private String id;
        List<PulseGeneratorService.WaveParam> params;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<PulseGeneratorService.WaveParam> getParams() {
            return params;
        }

        public void setParams(List<PulseGeneratorService.WaveParam> params) {
            this.params = params;
        }
    }
}
