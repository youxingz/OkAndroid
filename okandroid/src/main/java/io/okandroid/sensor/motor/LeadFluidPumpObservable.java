package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.exception.ModbusTransportException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class LeadFluidPumpObservable {

    private LeadFluidPump leadFluidPump;

    public LeadFluidPumpObservable(LeadFluidPump leadFluidPump) {
        this.leadFluidPump = leadFluidPump;
    }

    /**
     * 转速 RPM
     * 0x00 地址
     *
     * @return rpm
     */
    public Observable<Integer> velocity() {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(800); // ms
                    // 持续读
                    int speed = leadFluidPump.velocity();
                    if (emitter.isDisposed()) return;
                    emitter.onNext(speed);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    // emitter.onError(e);
                }
            }
        });
    }

    public Single<Integer> velocity(int velocity) {
        return Single.create(emitter -> {
            try {
                leadFluidPump.velocity(velocity);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(velocity);
            } catch (Exception e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 启停
     * 0x00
     *
     * @return
     */
    public Observable<Boolean> turn() {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(500); // ms
                    if (emitter.isDisposed()) return;
                    emitter.onNext(leadFluidPump.turn() == 1);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    // emitter.onError(e);
                }
            }
        });
    }

    public Single<Boolean> turn(boolean turnOn) {
        return Single.create(emitter -> {
            try {
                leadFluidPump.turn(turnOn ? 1 : 0);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(turnOn);
            } catch (Exception e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    /**
     * 正反
     * 0x01
     */
    public Observable<Integer> direction() throws ModbusTransportException {
        return Observable.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(500); // ms
                    int direction = leadFluidPump.direction();
                    if (emitter.isDisposed()) return;
                    emitter.onNext(direction);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    // emitter.onError(e);
                }
            }
        });
    }

    public Single<Integer> direction(int direction) throws ModbusTransportException {
        return Single.create(emitter -> {
            try {
                leadFluidPump.direction(direction);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(direction);
            } catch (Exception e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }
}
