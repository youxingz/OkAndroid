package io.okandroid.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.okandroid.exception.OkBluetoothException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class OkBluetoothClient {
    // Debugging
    private static final String TAG = "OK/OkBluetoothClient";
    // bluetooth:
    private final BluetoothAdapter mAdapter;
    private BluetoothSocket socket;
    private volatile boolean connectionIsWorking = false;
    private OutputStream out;
    // fields
    private BluetoothDevice device;
    private Type type;

    public enum Type {
        BondedDevice, NewFoundDevice, Unknown,
    }

    public enum ConnectionStatus {
        connecting, connected, disconnect, fail,
    }

    protected OkBluetoothClient(BluetoothDevice targetDevice, Type type) {
        this.device = targetDevice;
        this.type = type;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    public OkBluetoothClient(BluetoothDevice targetDevice) {
        this(targetDevice, (targetDevice.getBondState() == BluetoothDevice.BOND_BONDED) ? Type.BondedDevice : Type.Unknown);
    }

    /**
     * @param uuid   similarly: PORT with IP
     * @param secure
     * @return
     */
    @SuppressLint("MissingPermission")
    public Observable<ConnectionStatus> connect(UUID uuid, boolean secure) {
        return Observable.create(emitter -> {
            BluetoothSocket connectSocket = null;
            try {
                if (secure) {
                    connectSocket = device.createRfcommSocketToServiceRecord(uuid);
                } else {
                    connectSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                }
                if (!emitter.isDisposed()) emitter.onNext(ConnectionStatus.connecting);
                mAdapter.cancelDiscovery();
                connectSocket.connect(); // block until connected.
                socket = connectSocket;
                out = socket.getOutputStream(); // input stream 不需要初始化，谁读谁创建
                connectionIsWorking = true;
                if (!emitter.isDisposed()) emitter.onNext(ConnectionStatus.connected);
                while (connectionIsWorking) {
                    Thread.sleep(1000);
                }
                emitter.onNext(ConnectionStatus.disconnect);
            } catch (IOException | InterruptedException e) {
                // e.printStackTrace();
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
                try {
                    if (connectSocket != null) {
                        connectSocket.close();
                    }
                } catch (IOException e2) {
                    // e2.printStackTrace();
                    if (!emitter.isDisposed()) emitter.onError(e2);
                }
                if (!emitter.isDisposed()) {
                    emitter.onNext(ConnectionStatus.fail);
                    emitter.onComplete();
                }
            }
        });
    }

    public Observable<OkBluetoothMessage> read() {
        return Observable.create(emitter -> {
            if (socket == null) {
                emitter.onError(new OkBluetoothException("Please connect to this device first."));
                return;
            }
            // if (!socket.isConnected()) {
            if (!connectionIsWorking || !socket.isConnected()) {
                emitter.onError(new OkBluetoothException("Connection lost! Please connect to this device first."));
                return;
            }
            try (InputStream in = socket.getInputStream()) {
                int size = 0;
                byte[] buffer = new byte[1024];
                while (connectionIsWorking) {
                    if (emitter.isDisposed()) break;
                    size = in.read(buffer);
                    byte[] output = new byte[size];
                    System.arraycopy(buffer, 0, output, 0, size);
                    emitter.onNext(new OkBluetoothMessage(device, output, System.currentTimeMillis()));
                }
            } catch (IOException e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
            if (!emitter.isDisposed() || !connectionIsWorking) {
                emitter.onComplete();
            }
        });
    }

    public void disconnect() {
        connectionIsWorking = false;
        try {
            // output
            if (out != null) {
                out.close();
                out = null;
            }
            // socket
            if (socket != null) {
                if (!socket.isConnected()) return;
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Single<byte[]> write(byte[] data) {
        return Single.create(emitter -> {
            if (socket == null) {
                emitter.onError(new OkBluetoothException("Please connect to this device first."));
                return;
            }
            // if (!socket.isConnected()) {
            if (!connectionIsWorking) {
                emitter.onError(new OkBluetoothException("Connection lost! Please connect to this device first."));
                return;
            }
            try {
                out.write(data);
                emitter.onSuccess(data);
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    // getter

    public BluetoothDevice getDevice() {
        return device;
    }

    public Type getType() {
        return type;
    }
}
