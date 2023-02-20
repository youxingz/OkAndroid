package io.okandroid.bluetooth.reliable_protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;

import io.okandroid.bluetooth.OkBluetoothClient;
import io.okandroid.bluetooth.OkBluetoothMessage;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * command: line + \n\r
 */
public class SimpleStringProtocolClient implements ProtocolClient {
    private int timeout = 2000;
    private int retryTimes = 3;
    private OkBluetoothClient client;
    private boolean isReading = false;

    // looper
    private Hashtable<String, Request> requestPool;
    private Hashtable<String, Boolean> requestSend;
    private Hashtable<String, Response> responsePool;
    private Stack<String> responseStack;
    private Codecs encoder;

    public SimpleStringProtocolClient(OkBluetoothClient client, Codecs encoder) {
        this.client = client;
        this.requestPool = new Hashtable<>();
        this.requestSend = new Hashtable<>();
        this.responsePool = new Hashtable<>();
        this.responseStack = new Stack<>();
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
    public Single<Response> send(Request request) {
        return Single.create(emitter -> {
            if (!client.isConnecting()) {
                if (emitter.isDisposed()) return;
                emitter.onError(new ProtocolException("Please make sure the client is connecting."));
            }
            if (!isReading) startReading();
            // wait loop...
            int count = 0;
            while (count++ < retryTimes) {
                resend(request);
                // read back.
                long ddl = System.currentTimeMillis() + timeout;
                // timeout!
                while (System.currentTimeMillis() <= ddl) {
                    Thread.sleep(10);
                    synchronized (SimpleStringProtocolClient.class) {
                        Boolean sending = requestSend.get(request.getRequestId());
                        if (sending != null && sending) continue; // still sending...
                        Response resp = responsePool.get(request.getRequestId());
                        if (resp != null) {
                            // if (!resp.available()) continue;
                            // success!
                            if (emitter.isDisposed()) return;
                            emitter.onSuccess(resp);
                            responsePool.remove(request.getRequestId());
                            return;
                        }
                    }
                    Thread.sleep(40); // 10+40=50ms
                }
                // if (emitter.isDisposed()) return;
                // emitter.onError(new ProtocolException("Request Timeout! Try again."));
            }
            if (emitter.isDisposed()) return;
            emitter.onError(new ProtocolException("Request Timeout! " + (count * timeout) + " ms in total."));
        });
    }

    private void resend(Request request) {
        client.write(encoder.encode(request).getBytes(StandardCharsets.UTF_8)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<byte[]>() {
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
                String data = new String(okBluetoothMessage.getData(), Charset.defaultCharset());
                String lastPref = "";
                if (!responseStack.empty()) {
                    lastPref = responseStack.pop();
                    if (lastPref == null) lastPref = "";
                }
                String line = lastPref + data;
                if (!line.contains("\n") && !line.contains("\r")) {
                    // continue onNext.
                    responseStack.push(line);
                    return;
                }
                // success
                int index = line.indexOf("\n");
                if (index < 0) {
                    index = line.indexOf("\r");
                }
                String cmd = line.substring(0, index);
                String rest = line.substring(index).replaceAll("\n", "").replaceAll("\r", "");
                responseStack.push(rest);
                // resp.
                Response response = encoder.decode(cmd);
                if (response == null) return;
                // if (response.available()) {
                String reqId = response.getRequestId();
                Boolean sending = requestSend.get(reqId);
                if (sending != null) {
                    // finish it.
                    // System.out.println(">> finish: " + reqId);
                    responsePool.put(reqId, response);
                    requestSend.remove(reqId); // tag it
                }
                // }
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
        String encode(ProtocolClient.Request request);

        ProtocolClient.Response decode(String data);
    }

}
