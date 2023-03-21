package io.okandroid.sensor.motor;

import android.annotation.SuppressLint;

import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;

import io.okandroid.OkAndroid;
import io.okandroid.serial.modbus.Modbus;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;

public class LeadFluidPumpQueued {

    private Modbus master;
    private int slaveId;

    public LeadFluidPumpQueued(Modbus modbusMaster, int slaveId) {
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

    /**
     * 修改从机地址
     * 0x3107
     *
     * @param slaveAddressId
     * @return
     * @throws ModbusTransportException
     */
    public Single<Integer> address(int slaveAddressId) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 3107, slaveAddressId)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            observer.onSuccess(slaveAddressId);
        });
    }

    public Single<Integer> address() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 3107, 0x02)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess((int) ModbusUtils.toShort(data[0], data[1]));
        });
    }


    /**
     * 转速 1-6000 RPM
     * 1003 保持寄存器地址（掉电丢失）
     * 3100 保持寄存器地址（掉电可存）
     *
     * @return rpm
     */
    public Single<Integer> velocity() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 3100, 0x02)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess((int) ModbusUtils.toShort(data[0], data[1]));
        });
    }

    @SuppressLint("CheckResult")
    public Observable<Integer> velocityMulti(int interval) {
        // FunctionCode.READ_HOLDING_REGISTERS
        return Observable.create(emitter -> {
            while (emitter != null && !emitter.isDisposed()) {
                velocity().subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer velocity) {
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

    public Single<Integer> velocity(int velocity) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 3100, velocity)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            observer.onSuccess(velocity);
        });
    }


    public Single<int[]> directionAndVelocity(int velocity, int direction) throws ModbusTransportException {
        ModbusRequest requestV = new WriteRegisterRequest(slaveId, 3100, velocity);
        ModbusRequest requestD = new WriteRegisterRequest(slaveId, 3101, direction);
        return master.sendQueued(requestV, requestD).flatMap(modbusResponse -> (SingleSource<int[]>) observer -> {
            observer.onSuccess(new int[]{velocity, direction});
        });
        // return master.sendQueued(new WriteRegistersRequest(slaveId, 3100, new short[]{velocity, direction})).flatMap(modbusResponse -> (SingleSource<short[]>) observer -> {
        //     observer.onSuccess(new short[]{velocity, direction});
        // });
    }

    /**
     * 正反, 0: 逆时针, 1: 顺时针
     * 3101
     */
    public Single<Integer> direction() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 3101, 0x02)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
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
        return master.sendQueued(new WriteRegisterRequest(slaveId, 3101, direction)).flatMap(modbusResponse -> (SingleSource<Integer>) observer -> {
            observer.onSuccess(direction);
        });
    }

    /**
     * 启停, 0: 停止, 1: 启动
     * 3102
     */
    public Single<Boolean> turn() throws ModbusTransportException {
        // FunctionCode.READ_HOLDING_REGISTERS
        return master.sendQueued(new ReadHoldingRegistersRequest(slaveId, 3102, 0x02)).flatMap(modbusResponse -> (SingleSource<Boolean>) observer -> {
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) modbusResponse.getResponse();
            byte[] data = response.getData();
            observer.onSuccess(ModbusUtils.toShort(data[0], data[1]) == 1);
        });
    }

    public Single<Boolean> turn(boolean turnOn) throws ModbusTransportException {
        // FunctionCode.WRITE_REGISTER
        return master.sendQueued(new WriteRegisterRequest(slaveId, 3102, turnOn ? 1 : 0)).flatMap(modbusResponse -> (SingleSource<Boolean>) observer -> {
            observer.onSuccess(turnOn);
        });
    }

}
