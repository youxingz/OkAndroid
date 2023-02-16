package io.okandroid.sample;

import android.app.Activity;

import io.okandroid.bluetooth.OkBluetoothScanner;
import io.okandroid.bluetooth.OkBluetoothClient;
import io.okandroid.bluetooth.OkBluetoothMessage;
import io.okandroid.exception.OkAndroidException;
import io.okandroid.bluetooth.OkBluetoothException;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BluetoothTest {
    //    @Test
    public void client() throws OkBluetoothException.BluetoothNotEnableException, OkBluetoothException {
        OkBluetoothScanner client = new OkBluetoothScanner(new Activity());
        final OkBluetoothClient[] okDevice = {null};
        client.scan(true).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<OkBluetoothClient>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull OkBluetoothClient okBluetoothClient) {
                if (okDevice[0] == null) {
                    okDevice[0] = okBluetoothClient;
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        okDevice[0].read().subscribe(new Observer<OkBluetoothMessage>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull OkBluetoothMessage okBluetoothMessage) {
                System.out.println(okBluetoothMessage.getData().length);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
