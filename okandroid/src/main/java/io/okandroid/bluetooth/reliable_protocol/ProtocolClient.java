package io.okandroid.bluetooth.reliable_protocol;

import io.reactivex.rxjava3.core.Single;

/**
 *
 */
public interface ProtocolClient {

    ProtocolClient withTimeout(int ms);

    ProtocolClient withRetry(int times);

    Single<Response> send(Request request);

    public interface Request {
        String getRequestId();
    }

    public interface Response {

        boolean available();

        abstract String getRequestId();
    }

    public class ProtocolException extends Exception {
        public ProtocolException(String message) {
            super(message);
        }
    }

}
