package io.okandroid.exception;

public class OkBluetoothException {
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
}
