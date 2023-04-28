package com.cardioflex.motor.leadfluid_x;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cardioflex.motor.R;
import com.google.android.material.snackbar.Snackbar;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusTransportException;

import java.io.IOException;

import io.okandroid.OkAndroid;
import io.okandroid.sensor.motor.LeadFluidPumpQueued;
import io.okandroid.serial.SerialDevice;
import io.okandroid.serial.modbus.Modbus;
import io.okandroid.serial.modbus.ModbusMasterCreator;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class LiquidActivity extends AppCompatActivity {

    public static ConfigListAdapter.ConfigModel currentModel;
    private ConfigListAdapter adapter;

    private LeadFluidPumpQueued pump1;
    private LeadFluidPumpQueued pump2;

    private static volatile boolean working;

    ListView listView;
    Button addItemBtn;
    ToggleButton stopOrStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquid);
        initView();
        try {
            initSerial();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loop() {
        OkAndroid.newThread().scheduleDirect(() -> {
            Snackbar.make(listView, "循环已开启", Snackbar.LENGTH_LONG).show();
            while (true) {
                try {
                    if (!working) {
                        Snackbar.make(listView, "循环结束", Snackbar.LENGTH_LONG).show();
                        pump1.clearQueue();
                        pump2.clearQueue();
                        break;
                    }
                    for (int i = 0; i < adapter.getCount(); i++) {
                        ConfigListAdapter.ConfigModel model = adapter.getItem(i);
                        LeadFluidPumpQueued pump = "泵A".equals(model.pumpName) ? pump1 : pump2;
                        if (model.minutes == 0) {
                            continue;
                        }
                        // stop others
                        if (pump == pump1) {
                            pump2.turn(false).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {

                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                    Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            pump1.turn(false).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {

                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                    Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                        // start self.
                        pump.turn(true).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull Boolean aBoolean) {

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                e.printStackTrace();
                                Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                        // direction
                        pump.direction(model.isClockwise ? 1 : 0).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull Integer integer) {

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                e.printStackTrace();
                                Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                        // velocity
                        if (model.velocity == 0) {
                            // end it
                            pump.turn(false).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Boolean aBoolean) {
                                    // Snackbar.make(listView ,"", Snackbar.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                    Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            pump.velocity((int) (model.velocity * 10)).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onSuccess(@NonNull Integer integer) {

                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                    Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();

                                }
                            });
                        }
                        // time
                        long ms = model.minutes * 60000;
                        // pump.waitCommand(ms);
                        Thread.sleep(ms); // 自己也休息这么久，这样保证时间的一致性
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initSerial() throws IOException {
        TextView title = findViewById(R.id.title_text);
        String defaultDeviceName = new SerialPortFinder().getAllDevicesPath()[0]; // "/dev/ttyUSB0"
        title.setText("蠕动泵换液【" + defaultDeviceName + "】");
        SerialDevice device = SerialDevice.newBuilder(defaultDeviceName, 9600).dataBits(8).parity(2).stopBits(1).build();
        ModbusMaster modbusMaster = ModbusMasterCreator.create(device);
        modbusMaster.enableDebug(true);
        Modbus modbus = new Modbus(defaultDeviceName, modbusMaster);
        pump1 = new LeadFluidPumpQueued(modbus, 1);
        pump2 = new LeadFluidPumpQueued(modbus, 2);
    }

    private void initView() {
        listView = findViewById(R.id.config_list);
        addItemBtn = findViewById(R.id.config_item_add_btn);
        stopOrStart = findViewById(R.id.turn_btn);
        adapter = new ConfigListAdapter(this, R.layout.liquid_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // System.out.println(position);
            // currentModel = adapter.getItem(position);
            // AlertDialog.Builder builder = new AlertDialog.Builder(LiquidActivity.this);
            // View dialogView = LayoutInflater.from(this).inflate(R.layout.liquid_dialog, null);
            // builder.setView(dialogView);
            // builder.setTitle("配置编辑");
            // builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
            //     @Override
            //     public void onClick(DialogInterface dialog, int which) {
            //         String velocityStr = ((EditText) dialogView.findViewById(R.id.velocity_edit)).getText().toString();
            //         String timeStr = ((EditText) dialogView.findViewById(R.id.time_edit)).getText().toString();
            //         double velocity = 0.0;
            //         int time = 0;
            //         try {
            //             velocity = Double.parseDouble(velocityStr);
            //             time = Integer.parseInt(timeStr);
            //         } catch (NumberFormatException e) {
            //             e.printStackTrace();
            //         }
            //         currentModel.velocity = velocity;
            //         currentModel.minutes = time;
            //         adapter.notifyDataSetChanged();
            //     }
            // });
            // builder.show();
        });
        addItemBtn.setOnClickListener(view -> {
            adapter.add(new ConfigListAdapter.ConfigModel("泵A", 100.0, 60, true));
            adapter.notifyDataSetChanged();
        });
        stopOrStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                working = true;
                loop();
            } else {
                working = false;
                // end it.
                try {
                    pump1.turn(false).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            System.out.println("Pump1 关闭");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                            Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                    pump2.turn(false).subscribeOn(OkAndroid.subscribeIOThread()).observeOn(OkAndroid.mainThread()).subscribe(new SingleObserver<Boolean>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            System.out.println("Pump2 关闭");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                            Snackbar.make(listView, "错误：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                } catch (ModbusTransportException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        if (checked) {
            switch (view.getId()) {
                case R.id.direction_option_l:
                    // Pirates are the best
                    currentModel.isClockwise = true;
                    break;
                case R.id.direction_option_r:
                    // Ninjas rule
                    currentModel.isClockwise = false;
                    break;
                case R.id.pump_option_1:
                    currentModel.pumpName = "泵A";
                    break;
                case R.id.pump_option_2:
                    currentModel.pumpName = "泵B";
                    break;
            }
            // update
            adapter.notifyDataSetChanged();
        }
    }
}