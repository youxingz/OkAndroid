package io.okandroid.usb;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * <activity
 * android:name="..."
 * ...>
 * <intent-filter>
 * <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
 * </intent-filter>
 * <meta-data
 * android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
 * android:resource="@xml/device_filter" />
 * </activity>
 */
public class USB {

    public static final int READ_WRITE_TIMEOUT = 3000;
    private Activity activity;
    private UsbManager manager;
    private UsbSerialDriver driver;
    private UsbSerialPort port;

    public USB(Activity context) {
        this.activity = context;
    }

    /**
     * 115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE
     *
     * @param baudRate
     * @param dataBits
     * @param stopBits
     * @param party
     * @throws IOException
     */
    public void open(int baudRate, int dataBits, int stopBits, int party) throws IOException {
        // UsbSerialPort port = getPort();
        // Find all available drivers from attached devices.
        manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            throw new IOException("USB: connection setup fail.");
        }
        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        port.open(connection);
        port.setParameters(baudRate, dataBits, stopBits, party);
    }

    public byte[] read() throws IOException {
        byte[] buffer = new byte[1024];
        int len = port.read(buffer, READ_WRITE_TIMEOUT);
        byte[] data = new byte[len];
        System.arraycopy(buffer, 0, data, 0, len);
        return data;
    }

    public void write(byte[] data) throws IOException {
        port.write(data, READ_WRITE_TIMEOUT);
    }

    public void close() {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
