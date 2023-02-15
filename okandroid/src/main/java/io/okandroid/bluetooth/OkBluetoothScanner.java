package io.okandroid.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.okandroid.exception.OkAndroidException;
import io.okandroid.exception.OkBluetoothException;
import io.reactivex.rxjava3.core.Observable;

/**
 * Permissions Required:
 * <manifest ... >
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * <p>
 * <!-- If your app targets Android 9 or lower, you can declare
 * ACCESS_COARSE_LOCATION instead. -->
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * ...
 * </manifest>
 */
public class OkBluetoothScanner {
    private final static String TAG = "OK/BluetoothScanner";
    private Activity activity;
    public static int REQUEST_ENABLE_BT = 1;

    private BroadcastReceiver broadcastReceiver; // 蓝牙设备扫描使用

    public OkBluetoothScanner(Activity activity) {
        this.activity = activity;
    }

    // public void config() {
    // }

    public Observable<OkBluetoothClient> scan() throws OkAndroidException, OkBluetoothException.BluetoothNotEnableException {
        return scan(false);
    }

    @SuppressLint("MissingPermission")
    public Observable<OkBluetoothClient> scan(boolean gotoSettingIfNotTurnOn) throws OkAndroidException, OkBluetoothException.BluetoothNotEnableException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new OkAndroidException("Device does not support bluetooth");
        }
        if (!adapter.isEnabled()) {
            if (gotoSettingIfNotTurnOn) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                throw new OkBluetoothException.BluetoothNotEnableException("Bluetooth not enabled");
            }
        }
        return Observable.create(emitter -> {
            // if 5sec stub, emitter.onComplete!
            final long[] lastFoundDeviceAt = {System.currentTimeMillis()};
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() > lastFoundDeviceAt[0] + 5000) {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                        this.cancel();
                    }
                }
            }, 1000, 1000);
            // devices (no uuids)
            // Map<String, BluetoothDevice> noUuidDevices = new HashMap<>();
            broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    switch (action) {
                        case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                            // start scan.
                            break;
                        case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                            // end scan.
                            if (broadcastReceiver != null) {
                                activity.unregisterReceiver(broadcastReceiver);
                                broadcastReceiver = null;
                            }
                            if (emitter.isDisposed()) return;
                            emitter.onComplete();
                            break;
                        }
                        case BluetoothDevice.ACTION_FOUND: {
                            lastFoundDeviceAt[0] = System.currentTimeMillis();
                            // Discovery has found a device. Get the BluetoothDevice
                            // object and its info from the Intent.
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (device == null) break;
                            // String deviceName = device.getName();
                            // String deviceHardwareAddress = device.getAddress(); // MAC address
                            if (device.getUuids() == null) {
                                // noUuidDevices.put(device.getAddress(), device);
                                device.fetchUuidsWithSdp();
                            } else {
                                if (emitter.isDisposed()) return;
                                emitter.onNext(new OkBluetoothClient(device, OkBluetoothClient.Type.NewFoundDevice));
                            }
                            break;
                        }
                        case BluetoothDevice.ACTION_UUID: {
                            BluetoothDevice deviceExtra = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                            OkBluetoothClient client = new OkBluetoothClient(deviceExtra, OkBluetoothClient.Type.NewFoundDevice);
                            if (uuidExtra != null) {
                                client.setUuids(uuidExtra);
                                // for (Parcelable p : uuidExtra) {
                                // System.out.println("uuidExtra - " + p);
                                // }
                            }
                            // else {
                            // System.out.println("uuidExtra is still null");
                            // deviceExtra.fetchUuidsWithSdp();
                            // return;
                            // }
                            if (emitter.isDisposed()) return;
                            emitter.onNext(client);
                            // noUuidDevices.remove(deviceExtra.getAddress());
                        }
                    }
                }
            };
            // register receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);    // 开始扫描
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   // 扫描结束
            filter.addAction(BluetoothDevice.ACTION_FOUND);                 // 扫描中，返回结果
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);    // 扫描模式改变
            filter.addAction(BluetoothDevice.ACTION_UUID);                  // 获取UUID
            activity.registerReceiver(broadcastReceiver, filter);
            adapter.startDiscovery();
            // bonded devices
            Set<BluetoothDevice> list = adapter.getBondedDevices();
            for (BluetoothDevice device : list) {
                if (emitter.isDisposed()) return;
                emitter.onNext(new OkBluetoothClient(device, OkBluetoothClient.Type.BondedDevice));
            }
        });
    }
}
