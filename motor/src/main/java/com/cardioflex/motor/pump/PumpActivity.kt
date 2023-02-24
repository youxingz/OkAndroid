package com.cardioflex.motor.pump

import android.os.Bundle
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.cardioflex.motor.R
import io.okandroid.serial.SerialDevice

class PumpActivity : AppCompatActivity() {
    private lateinit var device: SerialDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pump)
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
            val pane = ControlPanePump(this, device, index + 1)
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