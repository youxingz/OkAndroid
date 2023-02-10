package io.okandroid;


import io.okandroid.bluetooth.OkBluetoothMessage;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

public class OkAndroid {
    public enum Type {
        Bluetooth,
        WiFi,
        Http
    }

    public Observable<?> create(Type type) {
        switch (type) {
            case Http: return Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                    emitter.onNext("");
                }
            }).observeOn(AndroidSchedulers.mainThread());
        }
        return null;
    }
    public void saySomething(String message) {
        System.out.println(message);
    }
}
