package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.exception.ModbusTransportException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * 雷塞步进电机驱动 modbus 协议
 */
public class LeadShinePr0Observable {
    private LeadShinePr0 pr0;

    public LeadShinePr0Observable(LeadShinePr0 leadShinePr0) {
        this.pr0 = leadShinePr0;
    }

    /**
     * 设置 Pr0 为速度模式
     */
    public Single<Boolean> setModeToVelocity() {
        return Single.create(emitter -> {
            try {
                pr0.setModeToVelocity();
                if (emitter.isDisposed()) return;
                emitter.onSuccess(true);
            } catch (ModbusTransportException e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 设置速度 0x6203
     *
     * @param velocity rpm
     */
    public Single<Integer> velocity(int velocity) {
        return Single.create(emitter -> {
            try {
                pr0.velocity(velocity);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(velocity);
            } catch (ModbusTransportException e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 读取速度
     *
     * @return rpm
     */
    public Observable<Integer> velocity() throws ModbusTransportException {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    int speed = pr0.velocity();
                    if (emitter.isDisposed()) return;
                    emitter.onNext(speed);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    emitter.onError(e);
                }
            }
        });
    }

    /**
     * 启动/急停
     *
     * @param turnOn {true} 启动, {false} 急停
     */
    public Single<Boolean> turn(boolean turnOn) {
        return Single.create(emitter -> {
            try {
                pr0.turn(turnOn);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(turnOn);
            } catch (ModbusTransportException e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 设置加减速时间 0x01E7
     *
     * @param time ms/Krmp
     */
    public Single<Integer> accelerationTime(boolean positive, int time) {
        return Single.create(emitter -> {
            try {
                pr0.accelerationTime(positive, time);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(time);
            } catch (ModbusTransportException e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 读取加减速时间
     *
     * @return ms/Krmp
     */
    public Observable<Integer> accelerationTime(boolean positive) {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    int speed = pr0.accelerationTime(positive);
                    if (emitter.isDisposed()) return;
                    emitter.onNext(speed);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    emitter.onError(e);
                }
            }
        });
    }
}
