package io.okandroid.serial;

import android.serialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialDevice {
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private File device;
    private int baudRate;
    private int dataBits = 8;
    private int parity = 0;
    private int stopBits = 1;
    private int flags = 0;

    private SerialDevice() {
    }

    /**
     * 串口
     *
     * @param device   串口设备文件
     * @param baudrate 波特率
     * @param dataBits 数据位；默认8,可选值为5~8
     * @param parity   奇偶校验；0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     * @param stopBits 停止位；默认1；1:1位停止位；2:2位停止位
     * @param flags    标志
     * @throws SecurityException
     * @throws IOException
     */
    public SerialDevice(File device, int baudrate, int dataBits, int parity, int stopBits, int flags) throws SecurityException, IOException {
        this.device = device;
        this.baudRate = baudrate;
        this.dataBits = dataBits;
        this.parity = parity;
        this.stopBits = stopBits;
        this.flags = flags;
    }

    public void open() throws IOException {
        SerialPort.setSuPath("/system/xbin/su");
        this.serialPort = new SerialPort(device, baudRate, dataBits, parity, stopBits, flags);
        this.inputStream = serialPort.getInputStream();
        this.outputStream = serialPort.getOutputStream();
    }

    public File getDevice() {
        return device;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getParity() {
        return parity;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getFlags() {
        return flags;
    }

    public void close() {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inputStream = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            outputStream = null;
        }
    }

    // builder

    public static Builder newBuilder(File device, int baudrate) {
        return new Builder(device, baudrate);
    }

    public static Builder newBuilder(String devicePath, int baudrate) {
        return new Builder(devicePath, baudrate);
    }

    public static class Builder {
        private File device;
        private int baudrate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        private Builder(File device, int baudrate) {
            this.device = device;
            this.baudrate = baudrate;
        }

        /**
         * 数据位
         *
         * @param dataBits 默认8,可选值为5~8
         * @return
         */
        public SerialDevice.Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        /**
         * 校验位
         *
         * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
         * @return
         */
        public SerialDevice.Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * 停止位
         *
         * @param stopBits 默认1；1:1位停止位；2:2位停止位
         * @return
         */
        public SerialDevice.Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        /**
         * 标志
         *
         * @param flags 默认0
         * @return
         */
        public SerialDevice.Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * 打开并返回串口
         *
         * @return
         * @throws SecurityException
         * @throws IOException
         */
        public SerialDevice build() throws SecurityException, IOException {
            return new SerialDevice(device, baudrate, dataBits, parity, stopBits, flags);
        }
    }
}
