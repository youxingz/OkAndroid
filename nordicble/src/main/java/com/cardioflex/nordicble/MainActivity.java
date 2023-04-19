package com.cardioflex.nordicble;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.okandroid.OkAndroid;
import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.bluetooth.le.OkBleScanner;
import io.okandroid.bluetooth.le.service.PulseGeneratorService;
import io.okandroid.cardioflex.pulsegen.Nordic52832;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkPermission()) {
                scan();
            }
        }

    }

    private Disposable disposable;

    private void scan() {

        OkBleScanner scanner = new OkBleScanner(this);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(PulseGeneratorService.PULSE_GENERATOR_SERVICE)).build();
        System.out.println("?");
        disposable = scanner.scan(new ScanSettings.Builder().build(), Collections.singletonList(filter)).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread()).subscribe(new Consumer<ScanResult>() {
            @Override
            public void accept(ScanResult scanResult) throws Throwable {
                Log.i(TAG, "find one...");
                System.out.println(scanResult.getScanRecord().getDeviceName());
                String mac = scanResult.getDevice().getAddress();
                System.out.println(mac);
                // F5:34:F4:78:DB:AA
                // D6:78:A9:EE:65:3F
                if ("F5:34:F4:78:DB:AA".equals(mac)) {
                    if (nordic52832 == null) {
                        nordic52832 = new Nordic52832(MainActivity.this, mac);
                        startClientJob();
                        scanner.stopScan();
                        if (disposable != null) {
                            disposable.dispose();
                        }
                    }
                }
            }
        });
    }

    private boolean notFirstConn;

    private void startClientJob() {
        Disposable disposableConnection = nordic52832.connect().subscribe(connectionStatus -> {
            Log.i(TAG, "connection::" + connectionStatus.name());
            if (connectionStatus == OkBleClient.ConnectionStatus.connected) {
                // if (!notFirstConn) {
                notFirstConn = true;
                nordic52832.requestMtu();
                Thread.sleep(2000);
                // System.out.println(client.getBluetoothGatt().getServices().size());
                startServiceJob();
                // }
            }
        });
    }

    private Nordic52832 nordic52832;

    private void startServiceJob() throws InterruptedException {
        TextView battery = findViewById(R.id.battery_text);
        nordic52832.deviceName().subscribe(s -> Log.i(TAG, "DEVICE_NAME: " + s));
        nordic52832.appearance().subscribe(integer -> Log.i(TAG, "APPEARANCE: " + integer));
        nordic52832.battery().subscribe(level -> {
            battery.setText("" + level);
            Log.i(TAG, "BATTERY_LEVEL: " + level);
        });
        nordic52832.currentWave().subscribe(ints -> System.out.println("WAVE:: " + Arrays.toString(ints)));
        List<PulseGeneratorService.WaveParam> params = new ArrayList<>();
        params.add(new PulseGeneratorService.WaveParam(PulseGeneratorService.WaveParam.Type.square, 7, new int[]{333, 444, 555, 666, 777, 888, 999}));
        params.add(new PulseGeneratorService.WaveParam(PulseGeneratorService.WaveParam.Type.exp, 7, new int[]{333, 444, 555, 666, 777, 888, 999}));
        params.add(new PulseGeneratorService.WaveParam(PulseGeneratorService.WaveParam.Type.pulse, 7, new int[]{333, 444, 555, 666, 777, 888, 999}));
        params.add(new PulseGeneratorService.WaveParam(PulseGeneratorService.WaveParam.Type.sin, 7, new int[]{333, 444, 555, 666, 777, 888, 999}));
        params.add(new PulseGeneratorService.WaveParam(PulseGeneratorService.WaveParam.Type.cos, 7, new int[]{333, 444, 555, 666, 777, 888, 999}));
        Thread.sleep(1000);
        nordic52832.sendWave(params).subscribe(new SingleObserver<List<BluetoothGattCharacteristic>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
                System.out.println(bluetoothGattCharacteristics.size());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            scan();
        }
    }
}