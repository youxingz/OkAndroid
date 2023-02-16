package io.okandroid.bluetooth;

public class OkBluetoothException extends Exception {
    public OkBluetoothException(String message) {
        super(message);
    }

    public static class BluetoothNotEnableException extends Exception {
        public BluetoothNotEnableException(String message) {
            super(message);
        }
    }

    public static class DeviceNotFoundException extends Exception {
        public DeviceNotFoundException(String message) {
            super(message);
        }
    }

    public static class DeviceWriteException extends Exception {
        private int code;

        public DeviceWriteException(String message) {
            super(message);
        }

        public DeviceWriteException(String message, int code) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static class ErrorCodeException extends Exception {
        private int code;

        public ErrorCodeException(int code) {
            super("Error: " + code);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
