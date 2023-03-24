package io.okandroid.cardioflex.pulsegen;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.List;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.service.BatteryService;
import io.okandroid.bluetooth.le.service.DeviceInformationService;
import io.okandroid.bluetooth.le.service.GenericAccessService;
import io.okandroid.bluetooth.le.service.PulseGeneratorService;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Nordic52832 {
    private OkBleClient client;
    private BatteryService batteryService;
    private DeviceInformationService deviceInformationService;
    private GenericAccessService genericAccessService;
    private PulseGeneratorService pulseGeneratorService;

    public Nordic52832(Context context, String macAddress) {
        this.client = new OkBleClient(context, macAddress);
        this.batteryService = new BatteryService(client);
        this.deviceInformationService = new DeviceInformationService(client);
        this.genericAccessService = new GenericAccessService(client);
        this.pulseGeneratorService = new PulseGeneratorService(client);
    }

    public Observable<OkBleClient.ConnectionStatus> connect() {
        return this.client.connect(true).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io());
    }

    public void disconnect() {
        this.client.disconnect();
    }

    public void requestMtu() { // set 500 (>480=16*30)
        client.setMtu(30).observeOn(OkAndroid.mainThread()).subscribeOn(Schedulers.io()).subscribe(new SingleObserver<Integer>() {
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

    /*** 具体服务 ***/

    /**
     * 电池电量信息
     *
     * @return 0-100
     */
    public Observable<Integer> battery() {
        return batteryService.batteryLevel().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Device ID
     *
     * @return hex byte[]
     */
    public Single<byte[]> deviceId() {
        return deviceInformationService.deviceId().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Manufacturer Name
     *
     * @return
     */
    public Single<String> manufacturerName() {
        return deviceInformationService.manufacturerName().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Model Number
     *
     * @return
     */
    public Single<String> modelNumber() {
        return deviceInformationService.modelNumber().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }


    /**
     * Device Name
     *
     * @return @String.
     */
    public Single<String> deviceName() {
        return genericAccessService.deviceName().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Device Name
     *
     * @param name 设备更新名称
     * @return
     */
    public Single<String> deviceName(String name) {
        return genericAccessService.deviceName(name).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Appearance
     *
     * @return @String, e.g. 960 (HID, Human Interface Device)
     */
    public Single<Integer> appearance() {
        return genericAccessService.appearance().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }


    /**
     * Peripheral Preferred Connection Parameters
     *
     * @return @String.
     */
    public Single<String> ppcp() {
        return genericAccessService.ppcp().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    /**
     * Central Address Resolution
     *
     * @return @String.
     */
    public Single<String> centralAddressResolution() {
        return genericAccessService.centralAddressResolution().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }


    public Observable<int[]> currentWave() {
        return pulseGeneratorService.currentWave().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }

    public Single<List<BluetoothGattCharacteristic>> sendWave(List<PulseGeneratorService.WaveParam> waveParams) {
        return pulseGeneratorService.sendWave(waveParams).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread());
    }
}
