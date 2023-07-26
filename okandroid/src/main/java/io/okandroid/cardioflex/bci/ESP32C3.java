package io.okandroid.cardioflex.bci;

import android.content.Context;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.service.BCIX16Service;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ESP32C3 {
    private OkBleClient client;
    private BCIX16Service bcix16Service;

    public ESP32C3(Context context, String macAddress) {
        this.client = new OkBleClient(context, macAddress);
        this.bcix16Service = new BCIX16Service(client);
    }

    public Observable<OkBleClient.ConnectionStatus> connect() {
        return this.client.connect(true).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io());
    }

    public void disconnect() {
        this.client.disconnect();
    }

    public void requestMtu(int mtu) { // set 500 (>480=16*30)
        client.setMtu(mtu).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull Integer integer) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    public @NonNull Observable<BCIX16Service.X16DataPayload> startSample(String secret) {
        return this.bcix16Service.startSample(secret).observeOn(OkAndroid.newThread()).subscribeOn(Schedulers.io());
    }

    public void stopSample() {
        this.bcix16Service.stopSample();
    }
}
