package io.okandroid.bluetooth.reliable_protocol;

import io.reactivex.rxjava3.core.Single;

/**
 * response = ProtocolClient().withRetry(5).send(request)
 */
public interface ProtocolClient {

    ProtocolClient withTimeout(int ms);

    ProtocolClient withRetry(int times);

    Response sendSync(Request request) throws ProtocolException;

    Single<Response> send(Request request);

    public interface Request {
        String getRequestId();
    }

    public interface Response {

        /**
         * @return {true} if this response is available, i.e., would not be ignored.
         */
        boolean available();

        abstract String getRequestId();
    }

    public class ProtocolException extends Exception {
        public ProtocolException(String message) {
            super(message);
        }
    }

}
