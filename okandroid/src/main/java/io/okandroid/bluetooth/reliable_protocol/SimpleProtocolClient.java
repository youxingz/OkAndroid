package io.okandroid.bluetooth.reliable_protocol;

import java.util.HashMap;

import io.okandroid.bluetooth.OkBluetoothClient;
import io.okandroid.bluetooth.OkBluetoothMessage;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SimpleProtocolClient implements ProtocolClient {
    private int timeout = 2000;
    private int retryTimes = 3;
    private OkBluetoothClient client;
    private boolean isReading = false;

    // looper
    private HashMap<Integer, Request> requestPool;
    private HashMap<Integer, Response> responsePool;
    private HashMap<Integer, Boolean> requestSend;
    private Codecs encoder;

    public SimpleProtocolClient(OkBluetoothClient client, Codecs encoder) {
        this.client = client;
        this.requestPool = new HashMap<>();
        this.requestSend = new HashMap<>();
        this.responsePool = new HashMap<>();
        this.encoder = encoder;
    }

    @Override
    public ProtocolClient withTimeout(int ms) {
        this.timeout = ms;
        return this;
    }

    @Override
    public ProtocolClient withRetry(int times) {
        this.retryTimes = times;
        return this;
    }

    @Override
    public Single<Response> send(Request request) throws ProtocolException {
        if (!client.isConnecting())
            throw new ProtocolException("Please make sure the client is connecting.");
        if (!isReading) startReading();
        return Single.create(emitter -> {
            // wait loop...
            int count = 0;
            while (count++ < retryTimes) {
                resend(request);
                // read back.
                long ddl = System.currentTimeMillis() + timeout;
                // timeout!
                while (System.currentTimeMillis() <= ddl) {
                    Boolean sending = requestSend.get(request.getRequestId());
                    if (sending != null && sending) continue; // still sending...
                    Response resp = responsePool.get(request.getRequestId());
                    if (resp != null) {
                        if (!resp.available()) continue;
                        // success!
                        emitter.onSuccess(resp);
                        responsePool.remove(request.getRequestId());
                        break;
                    }
                    Thread.sleep(50); // 50ms
                }
                emitter.onError(new ProtocolException("Request Timeout!"));
            }
        });
    }

    private void resend(Request request) {
        client.write(encoder.encode(request)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<byte[]>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                // sending...
            }

            @Override
            public void onSuccess(byte @NonNull [] bytes) {
                // read callback
                requestSend.put(request.getRequestId(), true);
                requestPool.put(request.getRequestId(), request);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                requestSend.put(request.getRequestId(), false);
                e.printStackTrace();
            }
        });
    }

    private void startReading() {
        isReading = true;
        client.read().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<OkBluetoothMessage>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                // start app.
            }

            @Override
            public void onNext(@NonNull OkBluetoothMessage okBluetoothMessage) {
                Response response = encoder.decode(okBluetoothMessage.getData());
                if (response.available()) {
                    int reqId = response.getRequestId();
                    Boolean sending = requestSend.get(reqId);
                    if (sending != null) {
                        // finish it.
                        requestSend.remove(reqId);
                    }
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                isReading = false;
            }
        });
    }


    /**
     * make sure you have implement this protocol.
     */
    public interface Codecs {
        byte[] encode(ProtocolClient.Request request);

        ProtocolClient.Response decode(byte[] data);
    }

}
