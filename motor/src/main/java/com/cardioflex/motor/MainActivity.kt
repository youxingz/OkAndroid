package com.cardioflex.motor

import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.okandroid.serial.SerialDevice

class MainActivity : AppCompatActivity() {
    lateinit var device: SerialDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "蠕动泵控制"
        init_modbus()
    }

    private fun init_modbus() {
        try {
            device = SerialDevice.newBuilder("/dev/ttyS8", 115200).build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val gridLayout: GridLayout = findViewById(R.id.control_container)
        for (index in IntArray(4) { it }) {
            val pane = ControlPane(this, device, index + 1)
            val params = GridLayout.LayoutParams()
            params.columnSpec = GridLayout.spec(index % 2)
            params.rowSpec = GridLayout.spec(index / 2)
//            pane.layoutParams = params
            gridLayout.addView(pane, params)
        }
//        gridLayout.addView(ControlPane(this, device, 0x01), 0)
//        gridLayout.addView(ControlPane(this, device, 0x02), 1)
//        gridLayout.addView(ControlPane(this, device, 0x03), 2)
//        gridLayout.addView(ControlPane(this, device, 0x04), 3)
    }
}