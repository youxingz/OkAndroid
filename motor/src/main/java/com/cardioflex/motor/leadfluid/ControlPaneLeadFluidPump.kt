package com.cardioflex.motor.leadfluid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.cardioflex.motor.R
import com.cardioflex.motor.utils.parseInt
import com.google.android.material.snackbar.Snackbar
import com.serotonin.modbus4j.exception.ModbusTransportException
import io.okandroid.OkAndroid
import io.okandroid.sensor.motor.LeadFluidPump
import io.okandroid.sensor.motor.LeadFluidPumpObservable
import io.okandroid.serial.SerialDevice
import io.okandroid.serial.modbus.Modbus
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ControlPaneLeadFluidPump(
    private val context: Activity,
    private val device: SerialDevice,
    private val slaveId: Int
) : LinearLayout(context) {

    private lateinit var motor: LeadFluidPumpObservable
    private lateinit var titleText: TextView
    private lateinit var btnTurn: ToggleButton

    // velocity
    private lateinit var velocityText: TextView
    private lateinit var velocityToggle: ToggleButton
    private lateinit var velocityEdit: EditText
    private lateinit var velocityDoneBtn: Button

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchDirection: Switch

    private lateinit var root: View

    private var disposableVelocity: Disposable? = null
    private var speed: Int = 0

    init {
        initModbus()
        initView()
    }

    private fun initModbus() {
        try {
            val modbus = Modbus(device)
            val modbusMaster = modbus.master()
            modbusMaster.retries = 5
            motor = LeadFluidPumpObservable(LeadFluidPump(modbusMaster, slaveId))
        } catch (e: ModbusTransportException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        root = inflater.inflate(R.layout.view_controlpane_leadfluidpump, this);
        // bind
        titleText = root.findViewById(R.id.title_text)
        btnTurn = root.findViewById(R.id.turn_btn)
        // velocity
        velocityText = root.findViewById(R.id.velocity_text)
        velocityToggle = root.findViewById(R.id.btn_velocity)
        velocityEdit = root.findViewById(R.id.velocity_edit)
        velocityDoneBtn = root.findViewById(R.id.btn_velocity_edit)
        // direction
        switchDirection = root.findViewById(R.id.direction_btn)

        val filename = device.device.name
        titleText.text = "蠕动泵【${slaveId}号 | 串口：$filename】"

        btnTurn.setOnCheckedChangeListener { v, isChecked ->
            if (speed == 0) {
                Snackbar.make(v, "请先设置速度", 0).show()
                btnTurn.isChecked = false
            } else {
                motor.turn(isChecked).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                    .subscribe({
                        Snackbar.make(
                            this, "【蠕动泵 $slaveId 号】${if (isChecked) "启动" else "急停"}成功", 0
                        ).show()
                    }, { e ->
                        e.printStackTrace()
//                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【蠕动泵 $slaveId 号】${if (isChecked) "启动" else "急停"}失败", 0)
                            .show()
                    })
            }
        }

        // velocity
        velocityToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose();
                }
                disposableVelocity = motor.velocity().subscribeOn(Schedulers.newThread())
                    .observeOn(OkAndroid.mainThread()).subscribe({
                        velocityText.text = "${it / 10} RPM"
                    }, { e ->
                        e.printStackTrace()
//                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【蠕动泵 $slaveId 号】速度读取失败，请重试", 0).show()
                        velocityToggle.isChecked = false
                    })
            } else {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose();
                }
            }
        }
        velocityDoneBtn.setOnClickListener { v ->
            val text = velocityEdit.text.toString()
            speed = parseInt(text)
            motor.velocity(speed).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                .subscribe({
                    Snackbar.make(v, "【蠕动泵 $slaveId 号】速度设置成功 [$speed] rpm", 0).show();
                }, { e ->
                    e.printStackTrace()
//                    e.localizedMessage?.let { context.appendLog(it) }
                    Snackbar.make(this, "【蠕动泵 $slaveId 号】速度设置失败", 0).show()
                })
        }
        // direction
        switchDirection.setOnCheckedChangeListener { _, isChecked ->
            motor.direction(if (isChecked) 1 else 0).subscribeOn(Schedulers.io())
                .observeOn(OkAndroid.mainThread()).subscribe({
                    Snackbar.make(this, "【蠕动泵 $slaveId 号】方向设置成功", 0).show()
                }, {
                    it.printStackTrace()
                    Snackbar.make(this, "【蠕动泵 $slaveId 号】方向设置失败", 0).show()
                })
        }
    }

}