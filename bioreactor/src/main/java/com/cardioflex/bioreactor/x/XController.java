package com.cardioflex.bioreactor.x;

import android.bluetooth.BluetoothGattCharacteristic;

import com.cardioflex.bioreactor.PageDashboard;
import com.cardioflex.bioreactor.PageDeviceList;
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

    /**
     * 脉搏蠕动泵设置
     *
     * @return
     */
    @PostMapping("/local-api/pump-pulse")
    public String pumpPulseConfig() {
        return "success";
    }

    /**
     * 换液蠕动泵设置
     *
     * @return
     */
    @PostMapping("/local-api/pump-liquid")
    public String pumpLiquidConfig() {
        return "success";
    }

    /**
     * 搅拌泵设置
     *
     * @return
     */
    @PostMapping("/local-api/pump-stir")
    public String pumpStirConfig() {
        return "success";
    }

}
