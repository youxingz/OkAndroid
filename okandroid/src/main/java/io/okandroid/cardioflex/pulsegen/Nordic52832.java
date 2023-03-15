package io.okandroid.cardioflex.pulsegen;

import android.content.Context;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.service.BatteryService;
import io.okandroid.bluetooth.le.service.DeviceInformationService;
import io.okandroid.bluetooth.le.service.GenericAccessService;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Nordic52832 {
    private OkBleClient client;
    private BatteryService batteryService;
    private DeviceInformationService deviceInformationService;
    private GenericAccessService genericAccessService;

    public Nordic52832(Context context, String macAddress) {
        this.client = new OkBleClient(context, macAddress);
        this.batteryService = new BatteryService(client);
        this.deviceInformationService = new DeviceInformationService(client);
        this.genericAccessService = new GenericAccessService(client);
    }

    public void connect() {
        this.client.connect(true).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<OkBleClient.ConnectionStatus>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                // start connecting...
            }

            @Override
            public void onNext(OkBleClient.@NonNull ConnectionStatus connectionStatus) {
                switch (connectionStatus) {
                    case connecting:
                    case connected:
                    case disconnecting:
                    case disconnected:
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                // connection end.
            }
        });
    }

    public void disconnect() {
        this.client.disconnect();
    }

    /*** 具体服务 ***/

    /**
     * 电池电量信息
     *
     * @return 0-100
     */
    public Observable<Integer> battery() {
        return batteryService.batteryLevel();
    }

    /**
     * Device ID
     *
     * @return hex byte[]
     */
    public Single<byte[]> deviceId() {
        return deviceInformationService.deviceId();
    }

    /**
     * Manufacturer Name
     *
     * @return
     */
    public Single<String> manufacturerName() {
        return deviceInformationService.manufacturerName();
    }

    /**
     * Model Number
     *
     * @return
     */
    public Single<String> modelNumber() {
        return deviceInformationService.modelNumber();
    }


    /**
     * Device Name
     *
     * @return @String.
     */
    public Single<String> deviceName() {
        return genericAccessService.deviceName();
    }

    /**
     * Device Name
     *
     * @param name 设备更新名称
     * @return
     */
    public Single<String> deviceName(String name) {
        return genericAccessService.deviceName(name);
    }

    /**
     * Appearance
     *
     * @return @String, e.g. 960 (HID, Human Interface Device)
     */
    public Single<Integer> appearance() {
        return genericAccessService.appearance();
    }


    /**
     * Peripheral Preferred Connection Parameters
     *
     * @return @String.
     */
    public Single<String> ppcp() {
        return genericAccessService.ppcp();
    }

    /**
     * Central Address Resolution
     *
     * @return @String.
     */
    public Single<String> centralAddressResolution() {
        return genericAccessService.centralAddressResolution();
    }
}
