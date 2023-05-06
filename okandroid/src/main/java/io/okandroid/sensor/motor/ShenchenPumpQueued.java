package io.okandroid.sensor.motor;

import android.annotation.SuppressLint;

import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegistersRequest;

import io.okandroid.OkAndroid;
import io.okandroid.serial.modbus.ModbusQueued;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;

public class ShenchenPumpQueued {

    private ModbusQueued master;
    private int slaveId;

    public ShenchenPumpQueued(ModbusQueued modbusMaster, int slaveId) {
        this.master = modbusMaster;
        this.slaveId = slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public Single<Long> waitCommand(long millis) {
        return master.waitQueued(millis).flatMap(modbusResponse -> (SingleSource<Long>) observer -> {
            observer.onSuccess(millis);
        });
    }

    public void clearQueue() {
        master.clearQueue();
    }

    /**
     * 修改从机地址
     * 2000
     *
     * @param slaveAddressId 1-32
     * @return
     * @throws ModbusTransportException
     */
    public Single<Integer> address(int slaveAddressId) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 2000, slaveAddressId)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            observer.onSuccess(slaveAddressId);
        });
    }

    public Single<Integer> address() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 2000, 0x02)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            if (modbusResponse.getResponse() == null) {
                observer.onSuccess(0);
                return;
            }
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess((int) ModbusUtils.toShort(data[0], data[1]));
        });
    }


    /**
     * 转速 1-6000 RPM
     * 1002 保持寄存器地址
     *
     * @return rpm
     */
    public Single<Float> velocity() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 1002, 0x04)).flatMap(modbusResponse -> (SingleSource<Float>) observer -> {
            if (modbusResponse.getResponse() == null) {
                observer.onSuccess(0f);
                return;
            }
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            int dataBits = data[0] << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
            observer.onSuccess(Float.intBitsToFloat(dataBits));
            // observer.onSuccess((int) ModbusUtils.toShort(data[0], data[1]));
        });
    }

    @SuppressLint("CheckResult")
    public Observable<Float> velocityMulti(int interval) {
        // FunctionCode.READ_HOLDING_REGISTERS
        return Observable.create(emitter -> {
            while (emitter != null && !emitter.isDisposed()) {
                velocity().subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Float>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Float velocity) {
                        if (emitter.isDisposed()) return;
                        emitter.onNext(velocity);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (emitter.isDisposed()) return;
                        emitter.onError(e);
                    }
                });
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    if (emitter.isDisposed()) return;
                    emitter.onError(e);
                }
            }
        });
    }

    public Single<Float> velocity(float velocity) throws ModbusTransportException {
        int intBits = Float.floatToIntBits(velocity);
        short[] data = new short[]{(short) (intBits >> 16), (short) (intBits)};
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegistersRequest(slaveId, 1002, data)).flatMap(modbusResponse -> (SingleSource<Float>) observer -> {
            observer.onSuccess(velocity);
        });
    }

    public Single<short[]> directionAndVelocityAndTurnOn(int velocity, int direction, int turnOn) throws ModbusTransportException {
        // ModbusRequest requestV = new WriteRegisterRequest(slaveId, 1002, velocity);
        // ModbusRequest requestD = new WriteRegisterRequest(slaveId, 1001, direction);
        // return master.sendQueued(requestV, requestD).flatMap(modbusResponse -> (SingleSource<int[]>) observer -> {
        //     observer.onSuccess(new int[]{velocity, direction});
        // });
        int intBits = Float.floatToIntBits(velocity);
        short[] data = new short[]{(short) turnOn, (short) (direction), (short) (intBits >> 16), (short) (intBits)};
        return master.sendQueued(new WriteRegistersRequest(slaveId, 1000, data)).flatMap(modbusResponse -> (SingleSource<short[]>) observer -> {
            observer.onSuccess(data);
        });
    }

    /**
     * 正反, 0: 逆时针, 1: 顺时针
     * 1001
     */
    public Single<Integer> direction() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 1001, 0x02)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            if (modbusResponse.getResponse() == null) {
                observer.onSuccess(0);
                return;
            }
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess((int) ModbusUtils.toShort(data[0], data[1]));
        });
    }

    @SuppressLint("CheckResult")
    public Observable<Integer> directionMulti(int interval) {
        // FunctionCode.READ_HOLDING_REGISTERS
        return Observable.create(emitter -> {
            while (emitter != null && !emitter.isDisposed()) {
                direction().subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer direction) {
                        if (emitter.isDisposed()) return;
                        emitter.onNext(direction);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (emitter.isDisposed()) return;
                        emitter.onError(e);
                    }
                });
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    if (emitter.isDisposed()) return;
                    emitter.onError(e);
                }
            }
        });
    }

    public Single<Integer> direction(int direction) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 1001, direction)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            observer.onSuccess(direction);
        });
    }

    /**
     * 启停, 0: 停止, 1: 启动
     * 1000
     */
    public Single<Boolean> turn() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 1000, 0x02)).flatMap(modbusResponse -> (SingleSource<Boolean>) observer -> {
            if (modbusResponse.getResponse() == null) {
                observer.onSuccess(false);
                return;
            }
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess(ModbusUtils.toShort(data[0], data[1]) == 1);
        });
    }

    public Single<Boolean> turn(boolean turnOn) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 1000, turnOn ? 1 : 0)).flatMap(modbusResponse -> (SingleSource<Boolean>) observer -> {
            observer.onSuccess(turnOn);
        });
    }

}
