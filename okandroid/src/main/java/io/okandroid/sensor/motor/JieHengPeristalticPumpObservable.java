package io.okandroid.sensor.motor;

import com.serotonin.modbus4j.exception.ModbusTransportException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class JieHengPeristalticPumpObservable {

    private JieHengPeristalticPump peristalticPump;

    public JieHengPeristalticPumpObservable(JieHengPeristalticPump peristalticPump) {
        this.peristalticPump = peristalticPump;
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
                    int speed = peristalticPump.velocity();
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
                peristalticPump.velocity(velocity);
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
                    emitter.onNext(peristalticPump.turn());
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
                peristalticPump.turn(turnOn);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(turnOn);
            } catch (Exception e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }

    public Single<Boolean> loopTurn(boolean turnOn) {
        return Single.create(emitter -> {
            while (true) {
                try {
                    Thread.sleep(1000); // ms
                    peristalticPump.turn(turnOn);
                    if (emitter.isDisposed()) return;
                    emitter.onSuccess(turnOn);
                } catch (Exception e) {
                    if (emitter.isDisposed()) return;
                    // emitter.onError(e);
                }
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
                    int direction = peristalticPump.direction();
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
                peristalticPump.direction(direction);
                if (emitter.isDisposed()) return;
                emitter.onSuccess(direction);
            } catch (Exception e) {
                if (emitter.isDisposed()) return;
                emitter.onError(e);
            }
        });
    }
}
