package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;

import io.okandroid.OkAndroid;
import io.okandroid.exception.OkModbusException;
import io.okandroid.serial.modbus.ModbusWithoutResp;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Emitter;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;

public class Leadshine57PumpQueued {

    private ModbusMaster master;
    private int slaveId;

    private static final int ADDRESS_VELOCITY = 0x01E1;
    private static final int ADDRESS_DIRECTION = 0x1801;

    private volatile boolean directionL2R = false;
    private volatile int velocity = 0;
    private volatile boolean working = false;

    public Leadshine57PumpQueued(ModbusMaster modbusMaster, int slaveId) {
        this.master = modbusMaster;
        this.slaveId = slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public void changeVelocity(int velocity) {
        this.velocity = velocity;
    }

    public void changeDirection(boolean direction) {
        directionL2R = direction;
    }

    /**
     * 启停, 0: 停止, 1: 启动
     * 0x000F
     */
    public Observable<Integer> turnOn() throws ModbusTransportException {
        return Observable.create(emitter -> {
            OkAndroid.subscribeIOThread().scheduleDirect(() -> {
                if (working) {
                    if (!emitter.isDisposed()) {
                        emitter.onComplete();
                    }
                    System.out.println("Plz stop working first.");
                    return;
                }
                working = true;
                int currentV = 0;
                boolean currentD = false;
                while (working) {
                    if (emitter.isDisposed()) {
                        break;
                    }
                    try {
                        if (currentV != velocity) {
                            currentV = velocity;
                            ModbusResponse response = master.send(new WriteRegisterRequest(slaveId, ADDRESS_VELOCITY, currentV));
                            if (!catchException(emitter, response)) {
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(currentV);
                                }
                            }
                        }
                        if (currentD != directionL2R) {
                            currentD = directionL2R;
                        }
                        ModbusResponse response = master.send(new WriteRegisterRequest(slaveId, ADDRESS_DIRECTION, currentD ? 0x4001 : 0x4002));
                        if (!catchException(emitter, response)) {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(currentV);
                            }
                        }
                        // Thread.sleep(40); // <50ms
                        Thread.sleep(20); // <50ms
                    } catch (ModbusTransportException | InterruptedException e) {
                        e.printStackTrace();
                        if (!emitter.isDisposed()) {
                            working = false;
                            emitter.onError(e);
                        }
                    }
                }
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            });
        });
    }

    public void turnOff() {
        working = false;
    }

    public boolean isWorking() {
        return working;
    }

    private boolean catchException(ObservableEmitter emitter, ModbusResponse response) {
        if (response.isException()) {
            // exception
            emitter.onError(new OkModbusException(response.getExceptionMessage()));
            return true;
        } else {
            // emitter.onNext(new ModbusWithoutResp.OkModbusResponse(response, true));
            return false;
        }
    }
}
