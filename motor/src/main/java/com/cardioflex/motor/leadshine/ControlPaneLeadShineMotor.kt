package com.cardioflex.motor.leadshine

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.cardioflex.motor.R
import com.google.android.material.snackbar.Snackbar
import com.serotonin.modbus4j.exception.ModbusTransportException
import io.okandroid.OkAndroid
import io.okandroid.sensor.motor.LeadShinePr0
import io.okandroid.sensor.motor.LeadShinePr0Observable
import io.okandroid.serial.SerialDevice
import io.okandroid.serial.modbus.Modbus
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ControlPaneLeadShineMotor(
    private val context: LeadShineActivity,
    private val device: SerialDevice,
    private val slaveId: Int
) : LinearLayout(context) {

    private lateinit var motor: LeadShinePr0Observable
    private lateinit var titleText: TextView
    private lateinit var btnTurn: ToggleButton

    // velocity
    private lateinit var velocityText: TextView
    private lateinit var velocityToggle: ToggleButton
    private lateinit var velocityEdit: EditText
    private lateinit var velocityDoneBtn: Button

    // acc positive
    private lateinit var accPositiveText: TextView
    private lateinit var accPositiveToggle: ToggleButton
    private lateinit var accPositiveEdit: EditText
    private lateinit var accPositiveDoneBtn: Button

    // acc negative
    private lateinit var accNegativeText: TextView
    private lateinit var accNegativeToggle: ToggleButton
    private lateinit var accNegativeEdit: EditText
    private lateinit var accNegativeDoneBtn: Button

    private lateinit var root: View

    private var disposableVelocity: Disposable? = null
    private var disposableAccPositive: Disposable? = null
    private var disposableAccNegative: Disposable? = null
    private var speed: Int = 0

    init {
        initModbus()
        initView()
    }

    private fun initModbus() {
        try {
            val modbus = Modbus(device)
            val modbusMaster = modbus.master()
            modbusMaster.retries = 3
            motor = LeadShinePr0Observable(LeadShinePr0(modbusMaster, slaveId))
            motor.setModeToVelocity().subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                .subscribe({}, { e ->
                    e.printStackTrace()
                    e.localizedMessage?.let { context.appendLog(it) }
                    Snackbar.make(this, "【电机 $slaveId 号】速度模式设置失败", 0).show()
                })
        } catch (e: ModbusTransportException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        root = inflater.inflate(R.layout.view_controlpane_leadshine, this);
        // bind
        titleText = root.findViewById(R.id.title_text)
        btnTurn = root.findViewById(R.id.turn_btn)
        // velocity
        velocityText = root.findViewById(R.id.velocity_text)
        velocityToggle = root.findViewById(R.id.btn_velocity)
        velocityEdit = root.findViewById(R.id.velocity_edit)
        velocityDoneBtn = root.findViewById(R.id.btn_velocity_edit)
        // acc+
        accPositiveText = root.findViewById(R.id.acc_positive_text)
        accPositiveToggle = root.findViewById(R.id.btn_acc_positive)
        accPositiveEdit = root.findViewById(R.id.acc_positive_edit)
        accPositiveDoneBtn = root.findViewById(R.id.btn_acc_positive_edit)
        // acc-
        accNegativeText = root.findViewById(R.id.acc_negative_text)
        accNegativeToggle = root.findViewById(R.id.btn_acc_negative)
        accNegativeEdit = root.findViewById(R.id.acc_negative_edit)
        accNegativeDoneBtn = root.findViewById(R.id.btn_acc_negative_edit)

        val filename = device.device.name
        titleText.text = "电机【${slaveId}号 | 串口：$filename】"

        btnTurn.setOnCheckedChangeListener { v, isChecked ->
            if (speed == 0) {
                Snackbar.make(v, "请先设置速度", 0).show()
                btnTurn.isChecked = false
            } else {
                motor.turn(isChecked).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                    .subscribe({
                        Snackbar.make(
                            this, "【电机 $slaveId 号】${if (isChecked) "启动" else "急停"}成功", 0
                        ).show()
                    }, { e ->
                        e.printStackTrace()
                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【电机 $slaveId 号】${if (isChecked) "启动" else "急停"}失败", 0)
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
                        velocityText.text = "$it RPM"
                    }, { e ->
                        e.printStackTrace()
                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【电机 $slaveId 号】速度读取失败，请重试", 0).show()
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
            speed = Integer.parseInt(text)
            motor.velocity(speed).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                .subscribe({
                    Snackbar.make(v, "【电机 $slaveId 号】速度设置成功 [$speed] rpm", 0).show();
                }, { e ->
                    e.printStackTrace()
                    e.localizedMessage?.let { context.appendLog(it) }
                    Snackbar.make(this, "【电机 $slaveId 号】速度设置失败", 0).show()
                })
        }
        // acc+
        accPositiveToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (disposableAccPositive != null && !disposableAccPositive!!.isDisposed) {
                    disposableAccPositive!!.dispose();
                }
                disposableAccPositive =
                    motor.accelerationTime(true).subscribeOn(Schedulers.newThread())
                        .observeOn(OkAndroid.mainThread()).subscribe({
                            accPositiveText.text = "$it ms/Krpm"
                        }, { e ->
                            e.printStackTrace()
                            e.localizedMessage?.let { context.appendLog(it) }
                            Snackbar.make(this, "【电机 $slaveId 号】加速时间读取失败，请重试", 0).show()
                            accPositiveToggle.isChecked = false
                        })
            } else {
                if (disposableAccPositive != null && !disposableAccPositive!!.isDisposed) {
                    disposableAccPositive!!.dispose();
                }
            }
        }
        accPositiveDoneBtn.setOnClickListener { v ->
            val text = accPositiveEdit.text.toString()
            val time = Integer.parseInt(text)
            motor.accelerationTime(true, time).subscribeOn(Schedulers.io())
                .observeOn(OkAndroid.mainThread()).subscribe({
                    Snackbar.make(v, "【电机 $slaveId 号】加速时间设置成功 [$speed] ms/Krpm", 0).show();
                }, { e ->
                    e.printStackTrace()
                    e.localizedMessage?.let { context.appendLog(it) }
                    Snackbar.make(this, "【电机 $slaveId 号】加速时间设置失败", 0).show()
                })
        }

        // acc-
        accNegativeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (disposableAccNegative != null && !disposableAccNegative!!.isDisposed) {
                    disposableAccNegative!!.dispose();
                }
                disposableAccNegative =
                    motor.accelerationTime(false).subscribeOn(Schedulers.newThread())
                        .observeOn(OkAndroid.mainThread()).subscribe({
                            accNegativeText.text = "$it ms/Krpm"
                        }, { e ->
                            e.printStackTrace()
                            e.localizedMessage?.let { context.appendLog(it) }
                            Snackbar.make(this, "【电机 $slaveId 号】减速时间读取失败，请重试", 0).show()
                            accNegativeToggle.isChecked = false
                        })
            } else {
                if (disposableAccNegative != null && !disposableAccNegative!!.isDisposed) {
                    disposableAccNegative!!.dispose();
                }
            }
        }
        accNegativeDoneBtn.setOnClickListener { v ->
            val text = accNegativeEdit.text.toString()
            val time = Integer.parseInt(text)
            motor.accelerationTime(false, time).subscribeOn(Schedulers.io())
                .observeOn(OkAndroid.mainThread()).subscribe({
                    Snackbar.make(v, "【电机 $slaveId 号】减速时间设置成功 [$speed] ms/Krpm", 0).show();
                }, { e ->
                    e.printStackTrace()
                    e.localizedMessage?.let { context.appendLog(it) }
                    Snackbar.make(this, "【电机 $slaveId 号】减速时间设置失败", 0).show()
                })
        }
    }

}