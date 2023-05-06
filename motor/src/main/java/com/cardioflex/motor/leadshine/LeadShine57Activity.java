package com.cardioflex.motor.leadshine;

import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cardioflex.motor.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lead_shine57);
        logTextView = findViewById(R.id.log_text);
        try {
            initSerial();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSerial() throws IOException {
        TextView title = findViewById(R.id.log_text);
        String defaultDeviceName = new SerialPortFinder().getAllDevicesPath()[0]; // "/dev/ttyUSB0"
        title.setText("蠕动泵换液【" + defaultDeviceName + "】\n");
        SerialDevice device = SerialDevice.newBuilder(defaultDeviceName, 38400).dataBits(8).parity(0).stopBits(1).build();
        ModbusMaster modbusMaster = ModbusMasterCreator.create(device);
        modbusMaster.enableDebug(true);
        ModbusWithoutResp modbus = new ModbusWithoutResp(defaultDeviceName, modbusMaster);
        motor = new Leadshine57PumpQueued(modbus, 1);
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
                            String message = System.currentTimeMillis() + "\t\t开始运行\n";
                            logTextView.setText(logTextView.getText() + message);
                        }

                        @Override
                        public void onNext(@NonNull Integer speed) {
                            if (speed != this.speed) {
                                this.speed = speed;
                                // current speed:
                                String message = System.currentTimeMillis() + "\t\t当前速度：" + speed + "\n";
                                logTextView.setText(logTextView.getText() + message);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                            String message = System.currentTimeMillis() + "\t\t" + e.getMessage() + "\n";
                            logTextView.setText(logTextView.getText() + message);
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
                    String message = System.currentTimeMillis() + "\t\t停止运行\n";
                    logTextView.setText(logTextView.getText() + message);
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
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
    }
}