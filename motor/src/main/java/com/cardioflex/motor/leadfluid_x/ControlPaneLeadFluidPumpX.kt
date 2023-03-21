package com.cardioflex.motor.leadfluid

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.cardioflex.motor.R
import com.cardioflex.motor.utils.parseInt
import com.google.android.material.snackbar.Snackbar
import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.exception.ModbusTransportException
import com.serotonin.modbus4j.msg.WriteRegisterRequest
import io.okandroid.OkAndroid
import io.okandroid.sensor.motor.LeadFluidPumpQueued
import io.okandroid.serial.SerialDevice
import io.okandroid.serial.modbus.Modbus
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ControlPaneLeadFluidPumpX(
    private val context: LeadFluidActivityX,
    private val device: SerialDevice,
    private var slaveId: Int
) : LinearLayout(context) {

    private lateinit var motor: LeadFluidPumpQueued
    private lateinit var titleText: TextView
    private lateinit var btnTurn: ToggleButton

    // velocity
    private lateinit var velocityText: TextView
    private lateinit var directionText: TextView
    private lateinit var velocityToggle: ToggleButton

    // 周期内
    private lateinit var velocityEdit1: EditText
    private lateinit var timeEdit1: EditText
    private lateinit var directionToggle1: ToggleButton
    private lateinit var velocityEdit2: EditText
    private lateinit var timeEdit2: EditText
    private lateinit var directionToggle2: ToggleButton

    // clear
    private lateinit var clearDirectionToggle: ToggleButton
    private lateinit var clearToggle: ToggleButton

    // address
    private lateinit var addressField: EditText
    private lateinit var addressBtn: Button

    private lateinit var root: View

    private var disposableVelocity: Disposable? = null
    private var disposableDirection: Disposable? = null

    private var working: Boolean = false
    private lateinit var modbusMaster: ModbusMaster

    private var isExceptionInWorking = false
    private var isExceptionInClearing = false

    init {
        initModbus()
        initView()
//        initTestBtn()
    }

    private fun initModbus() {
        try {
            val modbus = Modbus(device)
            modbusMaster = modbus.master()
            modbusMaster.retries = 2
            motor = LeadFluidPumpQueued(modbus, slaveId)
        } catch (e: ModbusTransportException) {
//            e.printStackTrace()
        }
    }

//    private fun initTestBtn() {
//        root.findViewById<Button>(R.id.button10).setOnClickListener { send(3101, 0) }
//        root.findViewById<Button>(R.id.button11).setOnClickListener { send(3101, 1) }
//        root.findViewById<Button>(R.id.button20).setOnClickListener { send(3102, 0) }
//        root.findViewById<Button>(R.id.button21).setOnClickListener { send(3102, 1) }
//
//        root.findViewById<Button>(R.id.buttonS0).setOnClickListener { send(3100, 0) }
//        root.findViewById<Button>(R.id.buttonS200).setOnClickListener { send(3100, 2000) }
//        root.findViewById<Button>(R.id.buttonS300).setOnClickListener { send(3100, 3000) }
//
//        root.findViewById<Button>(R.id.buttonT1)
//            .setOnClickListener { send(3100, 1234, 3101, 0) }
//        root.findViewById<Button>(R.id.buttonT2)
//            .setOnClickListener { send(3100, 2345, 3101, 1) }
//        root.findViewById<Button>(R.id.buttonT3)
//            .setOnClickListener { send(3101, 0, 3100, 2345) }
//        root.findViewById<Button>(R.id.buttonT4)
//            .setOnClickListener { send(3101, 1, 3100, 4567) }
//
//        root.findViewById<Button>(R.id.buttonC1).setOnClickListener {
//            motor.directionAndVelocity(222, 0).observeOn(OkAndroid.mainThread())
//                .subscribeOn(OkAndroid.subscribeIOThread()).subscribe({
//                    Snackbar.make(
//                        this, "success", 0
//                    ).show()
//                }, { e ->
//                    e.printStackTrace()
//                })
//        }
//        root.findViewById<Button>(R.id.buttonC2).setOnClickListener {
//            motor.directionAndVelocity(333, 1).observeOn(OkAndroid.mainThread())
//                .subscribeOn(OkAndroid.subscribeIOThread()).subscribe({
//                    Snackbar.make(
//                        this, "success", 0
//                    ).show()
//                }, { e ->
//                    e.printStackTrace()
//                })
//        }
//    }

    private fun send(address1: Int, value1: Int, address2: Int, value2: Int) {
        OkAndroid.newThread().scheduleDirect {
            send_impl(address1, value1)
            send_impl(address2, value2)
        }
    }

    private fun send(address: Int, value: Int) {
        OkAndroid.newThread().scheduleDirect {
            send_impl(address, value)
        }
    }

    private fun send_impl(address: Int, value: Int) {
        try {
            val request = WriteRegisterRequest(slaveId, address, value)
            val response = modbusMaster.send(request)
            if (response.isException) {
                OkAndroid.mainThread().scheduleDirect {
                    context.appendLog("[ERROR]" + response.exceptionMessage)
                }
            }
        } catch (e: ModbusTransportException) {
            OkAndroid.mainThread().scheduleDirect {
                e.localizedMessage?.let { context.appendLog(it) }
            }
        }
    }

    private var disposable: Disposable? = null

    @SuppressLint("ResourceType")
    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        root = inflater.inflate(R.layout.view_controlpane_leadfluidpump_x, this);
        // bind
        titleText = root.findViewById(R.id.title_text)
        btnTurn = root.findViewById(R.id.turn_btn)
        // velocity
        velocityText = root.findViewById(R.id.velocity_text)
        directionText = root.findViewById(R.id.direction_text)
        velocityToggle = root.findViewById(R.id.btn_velocity)
        velocityEdit1 = root.findViewById(R.id.velocity_edit1)
        timeEdit1 = root.findViewById(R.id.time_edit1)
        directionToggle1 = root.findViewById(R.id.direction_btn1)
        velocityEdit2 = root.findViewById(R.id.velocity_edit2)
        timeEdit2 = root.findViewById(R.id.time_edit2)
        directionToggle2 = root.findViewById(R.id.direction_btn2)
        // clear
        clearDirectionToggle = root.findViewById(R.id.direction_btn_clear)
        clearToggle = root.findViewById(R.id.clear_btn)
        // address
        addressBtn = root.findViewById(R.id.btn_set_address)
        addressField = root.findViewById(R.id.address_edit)
        addressField.setText("$slaveId")

        val filename = device.device.name
        titleText.text = "蠕动泵【${slaveId}号 | 串口：$filename】"

        btnTurn.setOnCheckedChangeListener { _, isChecked ->
            if (isExceptionInWorking) {
                isExceptionInWorking = false
                return@setOnCheckedChangeListener
            }
            motor.turn(isChecked).subscribeOn(Schedulers.io()).observeOn(OkAndroid.mainThread())
                .subscribe({
                    Snackbar.make(
                        this, "【蠕动泵 $slaveId 号】${if (isChecked) "启动" else "急停"}成功", 0
                    ).show()
                }, { e ->
                    Snackbar.make(
                        this, "【蠕动泵 $slaveId 号】${if (isChecked) "启动" else "急停"}失败", 0
                    ).show()
                    isExceptionInWorking = true
                    btnTurn.isChecked = false
                    working = false
                    e.printStackTrace()
                })
            working = isChecked
            // loop in thread
            if (isChecked) {
                disposable?.dispose()
                disposable = Schedulers.newThread().createWorker().schedule {
                    loop()
                    // make false
//                    OkAndroid.mainThread().createWorker().schedule {
//                        isExceptionInWorking = true
//                        btnTurn.isChecked = false
////                        modbusMaster.destroy()
////                        initModbus()
//                    }
                }
            }
        }

        // velocity read
        velocityToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose();
                }
                if (disposableDirection != null && !disposableDirection!!.isDisposed) {
                    disposableDirection!!.dispose()
                }
                disposableVelocity = motor.velocityMulti(200).subscribeOn(Schedulers.newThread())
                    .observeOn(OkAndroid.mainThread()).subscribe({
                        velocityText.text = "${it / 10} RPM"
                    }, { e ->
                        e.printStackTrace()
                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【蠕动泵 $slaveId 号】速度读取失败，请重试", 0).show()
                        velocityToggle.isChecked = false
                    })
                disposableDirection = motor.directionMulti(200).subscribeOn(Schedulers.newThread())
                    .observeOn(OkAndroid.mainThread()).subscribe({
                        directionText.text = if (it == 0) "顺时针↩️" else "逆时针↪️"
                    }, { e ->
                        e.printStackTrace()
                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(this, "【蠕动泵 $slaveId 号】方向读取失败，请重试", 0).show()
                        velocityToggle.isChecked = false
                    })
            } else {
                if (disposableVelocity != null && !disposableVelocity!!.isDisposed) {
                    disposableVelocity!!.dispose()
                }
                if (disposableDirection != null && !disposableDirection!!.isDisposed) {
                    disposableDirection!!.dispose()
                }
            }
        }
        // clear
        clearToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val direction = if (clearDirectionToggle.isChecked) 0 else 1
                setPeriodItem("-", 200, direction, 0)
            }
            try {
                if (isExceptionInClearing) {
                    isExceptionInClearing = false
                    return@setOnCheckedChangeListener
                }
                motor.turn(isChecked).subscribeOn(Schedulers.io())
                    .observeOn(OkAndroid.mainThread())
                    .subscribe({
                        Snackbar.make(
                            this, "【蠕动泵 $slaveId 号】高速${if (isChecked) "启动" else "急停"}成功", 0
                        ).show()
                    }, { e ->
                        e.printStackTrace()
                        e.localizedMessage?.let { context.appendLog(it) }
                        Snackbar.make(
                            this,
                            "【蠕动泵 $slaveId 号】高速${if (isChecked) "启动" else "急停"}失败",
                            0
                        ).show()
                        if (isChecked) { // 启动失败
                            isExceptionInClearing = true
                            clearToggle.isChecked = false
                        }
                    })
            } catch (e: ModbusTransportException) {
                // e.printStackTrace()
                e.localizedMessage?.let { context.appendLog(it) }
            }
        }
        clearDirectionToggle.setOnCheckedChangeListener { _, isChecked ->
            val direction = if (isChecked) 0 else 1
            setPeriodItem("-", 200, direction, 0)
        }

        // address
        addressBtn.setOnClickListener {
            val addressId = parseInt(addressField.text.toString())
            this.slaveId = addressId
            motor.address(addressId).observeOn(OkAndroid.mainThread())
                .subscribeOn(OkAndroid.subscribeIOThread())
                .subscribe({
                    motor.address().observeOn(OkAndroid.mainThread())
                        .subscribeOn(OkAndroid.subscribeIOThread()).subscribe({
                            Snackbar.make(
                                this,
                                "蠕动泵地址修改成功【新地址：$it 号】，请重启蠕动泵",
                                0
                            ).show()
                            motor.setSlaveId(addressId)
                        }, { e -> e.localizedMessage?.let { context.appendLog(it) } })
                }, { e ->
                    e.printStackTrace()
                    e.localizedMessage?.let { context.appendLog(it) }
                })
        }
    }


    @SuppressLint("CheckResult")
    private fun loop() {
        if (working) {
//            var success: Boolean
            val speed1 = parseInt(velocityEdit1.text.toString())
            val time1 = parseInt(timeEdit1.text.toString())
            val direction1 = if (directionToggle1.isChecked) 0 else 1
            val speed2 = parseInt(velocityEdit2.text.toString())
            val time2 = parseInt(timeEdit2.text.toString())
            val direction2 = if (directionToggle2.isChecked) 0 else 1

            setPeriodItem("1", speed1, direction1, time1.toLong()).subscribe({
                if (it is Int) {
                    return@subscribe
                }
                if (it is IntArray) {
                    val log =
                        "[1]: ${if (direction1 == 0) "顺时针" else "逆时针"} ($direction1) | $speed1 rpm"
                    Log.i("WORKING", log)
//                    context.appendLog("RUNNING: $log")
                    setPeriodItem("2", speed2, direction2, time2.toLong()).subscribe({
                        if (it is Int) {
                            return@subscribe
                        }
                        if (it is IntArray) {
                            val log =
                                "[2]: ${if (direction2 == 0) "顺时针" else "逆时针"} ($direction2) | $speed2 rpm"
                            Log.i("WORKING", log)
//                            context.appendLog("RUNNING: $log")
                            // loop
                            loop()
                        }
                    }, { e ->
                        e.localizedMessage?.let { context.appendLog(it) }
                        e.printStackTrace()
                        isExceptionInWorking = true
                        btnTurn.isChecked = false
                    })
                }
            }, { e ->
                e.localizedMessage?.let { context.appendLog(it) }
                e.printStackTrace()
                isExceptionInWorking = true
                btnTurn.isChecked = false
            })
//            Thread.sleep(time1.toLong())
//            Thread.sleep(time2.toLong())

//            Thread.sleep(time1.toLong())
//            Single.concat(
//                motor.direction(direction2),
//                motor.velocity(speed2 * 10)
//            ).observeOn(OkAndroid.mainThread()).subscribeOn(OkAndroid.subscribeIOThread())
//                .subscribe({
//                    val log = "[2]: $speed2 rpm, $time2 ms"
//                    Log.i("WORKING", log)
//                    context.appendLog("RUNNING: $log")
//                    // done
//                }, { e -> e.printStackTrace() })
            // wait time2 ms.
//            Thread.sleep(time2.toLong())
//            Log.i("WORKING", "working1...: $speed1 rpm, $time1 ms")
//            setPeriodItem(speed1, direction1)
//            if (!success) {
//                break
//            }
//            Thread.sleep(time1.toLong())
//            Log.i("WORKING", "working2...: $speed2 rpm, $time2 ms")
//            setPeriodItem(speed2, direction2)
//            if (!success) {
//                break
//            }
//            Thread.sleep(time2.toLong())
        }
    }

    // - direction: 1: 逆, 0: 顺
    @SuppressLint("CheckResult")
    private fun setPeriodItem(
        tag: String,
        speed: Int,
        direction: Int,
        time: Long
    ): Flowable<java.io.Serializable> {
        return Single.concat(
            motor.waitCommand(time),
            motor.directionAndVelocity(speed * 10, direction)
        )
//        motor.directionAndVelocity(speed * 10, direction)
            .observeOn(OkAndroid.mainThread())
            .subscribeOn(OkAndroid.subscribeIOThread())
    }

    private fun wait(time: Long): Single<Any> {
        return Single.create {
            Thread.sleep(time)
        }
    }
}