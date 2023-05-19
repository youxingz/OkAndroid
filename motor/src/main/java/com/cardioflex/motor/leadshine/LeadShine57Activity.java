package com.cardioflex.motor.leadshine;

import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.cardioflex.motor.R;
import com.google.android.material.snackbar.Snackbar;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import java.io.IOException;

import io.okandroid.OkAndroid;
import io.okandroid.sensor.motor.Leadshine57PumpQueued;
import io.okandroid.serial.SerialDevice;
import io.okandroid.serial.modbus.ModbusMasterCreator;
import io.okandroid.serial.modbus.ModbusWithoutResp;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class LeadShine57Activity extends AppCompatActivity {
    private Leadshine57PumpQueued motor;
    private TextView logTextView;
    private ToggleButton logEnableButton;
    private volatile boolean logEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_shine57);
        logTextView = findViewById(R.id.log_text);
        logEnableButton = findViewById(R.id.button_log_enable);
        try {
            initSerial();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logEnableButton.setOnCheckedChangeListener((buttonView, isChecked) -> logEnable = isChecked);
        logEnableButton.setChecked(true);
    }

    private volatile boolean working;

    private void loop() {
        OkAndroid.newThread().scheduleDirect(() -> {
            if (working) {
                Snackbar.make(logTextView, "循环已开启", Snackbar.LENGTH_SHORT).show();
                return;
            }
            working = true;
            long count = 0;
            while (working) {
                try {
                    if (!motor.isWorking()) {
                        Snackbar.make(logTextView, "循环开启", Snackbar.LENGTH_LONG).show();
                        motor.turnOn().observeOn(OkAndroid.mainThread()).subscribeOn(OkAndroid.subscribeIOThread()).subscribe(new Observer<Integer>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull Integer integer) {

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                working = false;
                                e.printStackTrace();
                                Snackbar.make(logTextView, "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                addLog("Error: " + e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                working = false;
                            }
                        });
                    }
                    motor.changeVelocity(120);
                    motor.changeDirection(true);
                    Thread.sleep(100);
                    motor.changeVelocity(10);
                    motor.changeDirection(false);
                    Thread.sleep(40);
                    String message = String.format("[LOOP:%6d]: 120RPM(100ms) + 10RPM(50ms)", count++);
                    OkAndroid.mainThread().scheduleDirect(() -> {
                        // logTextView.setText(logTextView.getText() + message);
                        addLog(message);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // throw new RuntimeException(e);
                } catch (ModbusTransportException e) {
                    e.printStackTrace();
                    // throw new RuntimeException(e);
                }
            }
            Snackbar.make(logTextView, "循环结束", Snackbar.LENGTH_LONG).show();
        });
    }

    private void initSerial() throws IOException {
        TextView title = findViewById(R.id.log_text);
        String defaultDeviceName = new SerialPortFinder().getAllDevicesPath()[0]; // "/dev/ttyUSB0"
        title.setText("蠕动泵57【" + defaultDeviceName + "】\n");
        SerialDevice device = SerialDevice.newBuilder(defaultDeviceName, 38400).dataBits(8).parity(0).stopBits(1).build();
        ModbusMaster modbusMaster = ModbusMasterCreator.create(device);
        modbusMaster.enableDebug(true);
        motor = new Leadshine57PumpQueued(modbusMaster, 1);
    }

    private Disposable disposable;

    public void onButtonClicked(View view) {
        try {
            switch (view.getId()) {
                case R.id.button_on: {
                    motor.turnOn().subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new Observer<Integer>() {
                        private int speed = -1;

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            disposable = d;
                            addLog("开始运行");
                        }

                        @Override
                        public void onNext(@NonNull Integer speed) {
                            if (speed != this.speed) {
                                this.speed = speed;
                                // current speed:
                                addLog("当前速度：" + speed);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                            addLog(e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                    break;
                }
                case R.id.button_off: {
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                    motor.turnOff();
                    addLog("停止运行");
                    break;
                }
                case R.id.button_dir_l2r: {
                    motor.changeDirection(true);
                    break;
                }
                case R.id.button_dir_r2l: {
                    motor.changeDirection(false);
                    break;
                }
                case R.id.button_v0: {
                    motor.changeVelocity(0);
                    // motor.velocity(0).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                    //     @Override
                    //     public void onSubscribe(@NonNull Disposable d) {
                    //
                    //     }
                    //
                    //     @Override
                    //     public void onSuccess(@NonNull Integer velocity) {
                    //
                    //     }
                    //
                    //     @Override
                    //     public void onError(@NonNull Throwable e) {
                    //         e.printStackTrace();
                    //         String message = System.currentTimeMillis() + "\t\t" + e.getMessage() + "\n";
                    //         logTextView.setText(logTextView.getText() + message);
                    //     }
                    // });
                    break;
                }
                case R.id.button_v300: {
                    motor.changeVelocity(300);
                    // motor.velocity(300).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                    //     @Override
                    //     public void onSubscribe(@NonNull Disposable d) {
                    //
                    //     }
                    //
                    //     @Override
                    //     public void onSuccess(@NonNull Integer velocity) {
                    //
                    //     }
                    //
                    //     @Override
                    //     public void onError(@NonNull Throwable e) {
                    //         e.printStackTrace();
                    //         String message = System.currentTimeMillis() + "\t\t" + e.getMessage() + "\n";
                    //         logTextView.setText(logTextView.getText() + message);
                    //     }
                    // });
                    break;
                }
                case R.id.button_ctrl_start: {
                    // start loop
                    loop();
                    break;
                }
                case R.id.button_ctrl_stop: {
                    working = false;
                    break;
                }
                case R.id.button_log_clear: {
                    logTextView.setText("");
                    break;
                }
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }

    private void addLog(String message) {
        if (logEnable) {
            String log = System.currentTimeMillis() + "\t\t" + message + "\n";
            logTextView.setText(logTextView.getText() + log);
        }
    }
}