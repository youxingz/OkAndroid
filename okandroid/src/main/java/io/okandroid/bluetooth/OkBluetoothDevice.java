package io.okandroid.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.okandroid.exception.OkBluetoothException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;

public class OkBluetoothDevice {
    // Debugging
    private static final String TAG = "OK/OkBluetoothDevice";
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "OkBluetoothDeviceSecure";
    private static final String NAME_INSECURE = "OkBluetoothDeviceInsecure";
    // Unique UUID for this application
//    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private ObservableEmitter<ConnectionStatus> connectEmitter;
    private ObservableEmitter<OkBluetoothMessage> readEmitter;
    private UUID uuid;
    // fields
    private BluetoothDevice device;
    private Type type;

    public enum Type {
        BondedDevice,
        NewFoundDevice,
    }

    public enum ConnectionStatus {
        connecting,
        connected,
        disconnect,
        connecting_lost,
        fail,
    }

    protected OkBluetoothDevice(BluetoothDevice device, Type type) {
        this.device = device;
        this.type = type;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Observable<ConnectionStatus> connect(boolean secure, UUID uuid) {
        return Observable.create(emitter -> {
            connectEmitter = emitter;
            connect(device, secure, uuid);
            if (emitter.isDisposed()) {
                connectEmitter = null;
            }
        });
    }

    public Observable<OkBluetoothMessage> read() {
        return Observable.create(emitter -> {
            readEmitter = emitter;
            if (mState != STATE_CONNECTED) {
                emitter.onComplete();
            }
            if (emitter.isDisposed()) {
                readEmitter = null;
            }
        });
    }

    public void disconnect() {
        stop_();
    }

    public void write(byte[] data) throws OkBluetoothException.DeviceWriteException {
        write_(data);
    }

    // getter

    public BluetoothDevice getDevice() {
        return device;
    }

    public Type getType() {
        return type;
    }

    //////// connect ////////

//    public interface Constants {
//
//        // Message types sent from the BluetoothChatService Handler
//        int MESSAGE_STATE_CHANGE = 1;
//        int MESSAGE_READ = 2;
//        int MESSAGE_WRITE = 3;
//        int MESSAGE_DEVICE_NAME = 4;
//        int MESSAGE_TOAST = 5;
//
//        // Key names received from the BluetoothChatService Handler
//        String DEVICE_NAME = "device_name";
//        String TOAST = "toast";
//
//    }


    // Member fields
    private final BluetoothAdapter mAdapter;
    //    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Return the current connection state.
     */
//    public synchronized int getState() {
//        return mState;
//    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    private synchronized void start() {
//        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private synchronized void connect(BluetoothDevice device, boolean secure, UUID uuid) {
//        Log.d(TAG, "connect to: " + device);
        this.uuid = uuid;
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure, uuid);
        mConnectThread.start();
        if (connectEmitter != null && !connectEmitter.isDisposed()) {
            connectEmitter.onNext(ConnectionStatus.connecting);
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @SuppressLint("MissingPermission")
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
//        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        if (connectEmitter != null && !connectEmitter.isDisposed()) {
            connectEmitter.onNext(ConnectionStatus.connected);
        }
        // Send the name of the connected device back to the UI Activity
//        if (mHandler == null) return;
//        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
    }

    /**
     * Stop all threads
     */
    private synchronized void stop_() {
//        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        if (connectEmitter != null && !connectEmitter.isDisposed()) {
            connectEmitter.onNext(ConnectionStatus.disconnect);
            connectEmitter.onComplete();
        }
        if (readEmitter != null && !readEmitter.isDisposed()) {
            readEmitter.onComplete();
        }
        mState = STATE_NONE;
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    private void write_(byte[] out) throws OkBluetoothException.DeviceWriteException {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        if (connectEmitter != null && !connectEmitter.isDisposed()) {
            connectEmitter.onNext(ConnectionStatus.fail);
        }
        // Send a failure message back to the Activity
//        if (mHandler == null) return;
//        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.TOAST, "Unable to connect device");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        mState = STATE_NONE;

        // Start the service over to restart listening mode
        this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        if (connectEmitter != null && !connectEmitter.isDisposed()) {
            connectEmitter.onNext(ConnectionStatus.connecting_lost);
        }
        // Send a failure message back to the Activity
//        if (mHandler == null) return;
//        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        mState = STATE_NONE;

        // Start the service over to restart listening mode
        this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    @SuppressLint("MissingPermission")
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
//                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, uuid);
                } else {
//                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, uuid);
                }
            } catch (IOException e) {
//                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
//            Log.d(TAG, "Socket Type: " + mSocketType +
//                    "BEGIN mAcceptThread" + this);
            setName("OK/AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
//                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    if (connectEmitter != null && !connectEmitter.isDisposed()) {
                        connectEmitter.onError(e);
                    }
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (OkBluetoothDevice.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
//                                    Log.e(TAG, "Could not close unwanted socket", e);
                                    if (connectEmitter != null && !connectEmitter.isDisposed()) {
                                        connectEmitter.onError(e);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
//            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        public void cancel() {
//            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
//                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    @SuppressLint("MissingPermission")
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure, UUID uuid) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
//                    tmp = device.createRfcommSocketToServiceRecord(
//                            MY_UUID_SECURE);
//                    tmp = device.createRfcommSocketToServiceRecord(
//                            device.getUuids()[0].getUuid());
                    tmp = device.createRfcommSocketToServiceRecord(uuid);
                } else {
//                    tmp = device.createInsecureRfcommSocketToServiceRecord(
//                            MY_UUID_INSECURE);
                    tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
//                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("OK/ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
//                e.printStackTrace();
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
//                    Log.e(TAG, "unable to close() " + mSocketType +
//                            " socket during connection failure", e2);
                    if (connectEmitter != null && !connectEmitter.isDisposed()) {
                        connectEmitter.onError(e);
                    }
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
//            synchronized (BluetoothService.this) {
            synchronized (OkBluetoothDevice.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
//                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
//            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
//                Log.e(TAG, "temp sockets not created", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    if (readEmitter != null && !readEmitter.isDisposed()) {
//                        String message = new String(buffer, 0, bytes);
//                        readEmitter.onNext(new OkBluetoothMessage(device, message.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis()));
                        // copy bytes
                        byte[] output = new byte[bytes];
                        System.arraycopy(buffer, 0, output, 0, bytes);
                        readEmitter.onNext(new OkBluetoothMessage(device, output, System.currentTimeMillis()));
                    }
                    // Send the obtained bytes to the UI Activity
//                    if (mHandler == null) break;
//                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
//                    Log.e(TAG, "disconnected", e);
                    if (connectEmitter != null && !connectEmitter.isDisposed()) {
                        connectEmitter.onError(e);
                    }
                    if (readEmitter != null && !readEmitter.isDisposed()) {
                        readEmitter.onError(e);
                    }
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) throws OkBluetoothException.DeviceWriteException {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
//                if (mHandler == null) return;
//                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
//                Log.e(TAG, "Exception during write", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
                throw new OkBluetoothException.DeviceWriteException(e.getMessage());
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
                if (connectEmitter != null && !connectEmitter.isDisposed()) {
                    connectEmitter.onError(e);
                }
            }
        }
    }
}
