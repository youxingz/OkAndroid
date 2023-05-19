package com.cardioflex.motor.leadshine;

import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
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
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class LeadShine57Activity extends AppCompatActivity {
    private Leadshine57PumpQueued motor;
    private TextView logTextView;
    private ToggleButton logEnableButton;
    private ScrollView logContainerScrollView;
    private volatile boolean logEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_shine57);
        logTextView = findViewById(R.id.log_text);
        logEnableButton = findViewById(R.id.button_log_enable);
        logContainerScrollView = findViewById(R.id.log_container);
        try {
            initSerial();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logEnableButton.setOnCheckedChangeListener((buttonView, isChecked) -> logEnable = isChecked);
        logEnableButton.setChecked(true);
        updateConfig(200, 300, true, 30, 60, false);
    }

    private volatile boolean working;

    private int v1 = 200;
    private int t1 = 300;
    private boolean d1 = true;
    private int v2 = 30;
    private int t2 = 60;
    private boolean d2 = false;

    public void updateConfig(int v1, int t1, boolean d1, int v2, int t2, boolean d2) {
        this.t1 = t1;
        this.t2 = t2;
        this.v1 = v1;
        this.v2 = v2;
        this.d1 = d1;
        this.d2 = d2;
        // scriptTurnOnButton.setText(String.format("开始脚本 P-%dRPM-%dms,N-%dRPM-%dms", v1, t1, v2, t2));
    }

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
                    motor.changeVelocity(v1);
                    motor.changeDirection(d1);
                    Thread.sleep(t1);
                    motor.changeVelocity(v2);
                    motor.changeDirection(d2);
                    Thread.sleep(t2);
                    String message = String.format("[LOOP:%6d]: %dRPM(%dms) + %dRPM(%dms)", count++, v1, t1, v2, t2);
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
                // case R.id.button_ctrl_start: {
                //     // start loop
                //     loop();
                //     break;
                // }
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

    public void onScriptButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.button_ctrl_start1: {
                updateConfig(300, 200, true, 10, 80, false);
                loop();
                break;
            }
            case R.id.button_ctrl_start2: {
                updateConfig(300, 200, true, 10, 80, true);
                loop();
                break;
            }
            case R.id.button_ctrl_start3: {
                updateConfig(200, 200, true, 40, 80, true);
                loop();
                break;
            }
            case R.id.button_ctrl_start4: {
                updateConfig(200, 200, true, 40, 80, false);
                loop();
                break;
            }
            case R.id.button_ctrl_start5: {
                updateConfig(120, 150, true, 10, 50, true);
                loop();
                break;
            }
            case R.id.button_ctrl_start6: {
                updateConfig(120, 150, true, 10, 50, false);
                loop();
                break;
            }
        }
    }

    private void addLog(String message) {
        if (logEnable) {
            String log = System.currentTimeMillis() + "\t\t" + message + "\n";
            logTextView.setText(logTextView.getText() + log);
            logContainerScrollView.fullScroll(View.FOCUS_DOWN);
        }
    }
}