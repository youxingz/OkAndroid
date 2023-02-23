package com.cardioflex.motor

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.serotonin.modbus4j.exception.ModbusTransportException
import io.okandroid.OkAndroid
import io.okandroid.sensor.motor.JieHengPeristalticPump
import io.okandroid.sensor.motor.JieHengPeristalticPumpObservable
import io.okandroid.serial.SerialDevice
import io.okandroid.serial.modbus.Modbus
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivityBackup : AppCompatActivity() {
    lateinit var pump: JieHengPeristalticPumpObservable
    lateinit var btnTurn: ToggleButton
//    lateinit var btnTurnContinue: ToggleButton

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    lateinit var switchDirection: Switch
    lateinit var textVelocity: TextView
    lateinit var btnVelocity: ToggleButton
    lateinit var btnVelocityEdit: Button
    lateinit var editVelocity: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "蠕动泵控制"
        modbus_init()
        init_views()
    }

    private var disposableVelocity: Disposable? = null

    //    private var disposableTurnOn: Disposable? = null
    private var speed: Int = 0

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun init_views() {
        btnTurn = findViewById(R.id.turn_btn)
        switchDirection = findViewById(R.id.direction_btn)
        textVelocity = findViewById(R.id.velocity_text)
        btnVelocity = findViewById(R.id.btn_velocity)
        btnVelocityEdit = findViewById(R.id.btn_velocity_edit)
        editVelocity = findViewById(R.id.velocity_edit)
//        btnTurnContinue = findViewById(R.id.turn_on_btn)

        btnTurn.setOnCheckedChangeListener { v, isChecked ->
            if (speed == 0) {
                Snackbar.make(v, "请先设置速度", 1).show()
                btnTurn.isChecked = false
            } else {
                pump.turn(isChecked).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                    .subscribe({}, { it.printStackTrace() })
            }
        }
        switchDirection.setOnCheckedChangeListener { _, isChecked ->
            pump.direction(if (isChecked) 1 else 0).subscribeOn(Schedulers.io())
                .observeOn(OkAndroid.mainThread()).subscribe({}, { it.printStackTrace() })
        }

        btnVelocity.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose();
                }
                disposableVelocity = pump.velocity().subscribeOn(Schedulers.newThread())
                    .observeOn(OkAndroid.mainThread()).subscribe({
                        textVelocity.text = "$it RPM"
                    }, { it.printStackTrace() })
            } else {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose();
                }
            }
        }
        btnVelocityEdit.setOnClickListener { v ->
            val text = editVelocity.text.toString()
            speed = Integer.parseInt(text)
            pump.velocity(speed).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                .subscribe({
                    Snackbar.make(v, "速度设置成功 [$speed] RPM", 1).show();
                }, { it.printStackTrace() })
        }
    }

    private fun modbus_init() {
        try {
            val modbus = Modbus(SerialDevice.newBuilder("/dev/ttyS8", 115200).build())
            val modbusMaster = modbus.master()
            modbusMaster.retries = 0
            pump = JieHengPeristalticPumpObservable(JieHengPeristalticPump(modbusMaster, 0x01))
        } catch (e: ModbusTransportException) {
            e.printStackTrace()
        }
    }

}