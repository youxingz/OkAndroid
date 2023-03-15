package com.cardioflex.motor.leadfluid

import android.os.Bundle
import android.serialport.SerialPortFinder
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.cardioflex.motor.DeviceListAdapter
import com.cardioflex.motor.R
import io.okandroid.serial.SerialDevice
import java.io.File

class LeadFluidActivity : AppCompatActivity() {
    private lateinit var adapter: DeviceListAdapter
    private lateinit var gridLayout: GridLayout
    private lateinit var logText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lead_fluid)
        initViews()
        // default set:
        updateDevice(File("/dev/ttyS8")) // default device
    }

    private fun initViews() {
        val deviceList: Spinner = findViewById(R.id.device_list)
        val refreshBtn: Button = findViewById(R.id.refresh_device_btn)
        gridLayout = findViewById(R.id.control_container)
        logText = findViewById(R.id.log_text)
        refreshBtn.setOnClickListener {
            refreshDeviceList()
        }
        adapter = DeviceListAdapter(this, R.layout.device_item)
        deviceList.adapter = adapter
        deviceList.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val device = adapter.getDevice(position)
                updateDevice(device)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        // after all:
        refreshDeviceList()
    }

    private fun updateDevice(deviceFile: File) {
        try {
            val device =
                SerialDevice.newBuilder(deviceFile, 9600).dataBits(8).parity(2).stopBits(1).build()
            gridLayout.removeAllViews()
            for (index in IntArray(3) { it }) {
                val pane = ControlPaneLeadFluidPump(this, device, index + 1)
                val params = GridLayout.LayoutParams()
                params.columnSpec = GridLayout.spec(index % 2)
                params.rowSpec = GridLayout.spec(index / 2)
                gridLayout.addView(pane, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.localizedMessage?.let { appendLog(it) }
        }
    }

    private fun refreshDeviceList() {
        adapter.clear()
        for (devicePath in SerialPortFinder().allDevicesPath) {
            adapter.addDevice(File(devicePath))
        }
        adapter.notifyDataSetChanged()
    }

    fun appendLog(text: String) {
        val log = "${System.currentTimeMillis()}\t$text\n"
        logText.text = logText.text.toString() + log
    }
}