package com.cardioflex.motor

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.serotonin.modbus4j.exception.ModbusTransportException
import io.okandroid.OkAndroid
import io.okandroid.sensor.motor.JieHengPeristalticPump
import io.okandroid.sensor.motor.JieHengPeristalticPumpObservable
import io.okandroid.serial.SerialDevice
import io.okandroid.serial.modbus.Modbus
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ControlPane(context: Context, val device: SerialDevice, val slaveId: Int) :
    LinearLayout(context) {

    lateinit var pump: JieHengPeristalticPumpObservable
    lateinit var titleText: TextView
    lateinit var btnTurn: ToggleButton

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    lateinit var switchDirection: Switch
    lateinit var textVelocity: TextView
    lateinit var btnVelocity: ToggleButton
    lateinit var btnVelocityEdit: Button
    lateinit var editVelocity: EditText

    lateinit var root: View

    private var disposableVelocity: Disposable? = null
    private var speed: Int = 0

    init {
        initModbus()
        initView()
        println("1234567890=====================")
    }

    private fun initModbus() {
        try {
            val modbus = Modbus(device)
            val modbusMaster = modbus.master()
            modbusMaster.retries = 0
            pump = JieHengPeristalticPumpObservable(JieHengPeristalticPump(modbusMaster, slaveId))
        } catch (e: ModbusTransportException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        root = inflater.inflate(R.layout.view_controlpane, this);
        // bind
        titleText = root.findViewById(R.id.title_text)
        btnTurn = root.findViewById(R.id.turn_btn)
        switchDirection = root.findViewById(R.id.direction_btn)
        textVelocity = root.findViewById(R.id.velocity_text)
        btnVelocity = root.findViewById(R.id.btn_velocity)
        btnVelocityEdit = root.findViewById(R.id.btn_velocity_edit)
        editVelocity = root.findViewById(R.id.velocity_edit)
//        btnTurnContinue = findViewById(R.id.turn_on_btn)

        titleText.text = "蠕动泵【${slaveId}号】"
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

}