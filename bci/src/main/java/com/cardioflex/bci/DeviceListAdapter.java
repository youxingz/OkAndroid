package com.cardioflex.bci;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private List<BluetoothDevice> devices;
    private FullscreenActivity context;

    public DeviceListAdapter(@NonNull FullscreenActivity context, int resource) {
        super(context, resource);
        devices = new ArrayList<>();
        this.context = context;
    }

    public void addDevice(BluetoothDevice device) {
        for (int i = 0; i < devices.size(); i++) {
            BluetoothDevice device_ = devices.get(i);
            if (device_ == null) continue;
            if (device.getAddress().equals(device_.getAddress())) {
                return;
            }
        }
        devices.add(device);
        this.add(device);
    }

    public BluetoothDevice getDevice(int position) {
        return devices.get(position);
    }


    @SuppressLint("MissingPermission")
    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.device_item, null, false);
        }
        BluetoothDevice device = getDevice(position);
        TextView title = convertView.findViewById(R.id.device_name);
        title.setText(device.getName());
        TextView address = convertView.findViewById(R.id.device_attr);
        address.setText("(" + device.getAddress() + ")");
        Button connectBtn = convertView.findViewById(R.id.start_conn_btn);
        connectBtn.setOnClickListener(v -> {
            context.worker.startConnDevice(device.getAddress());
        });
        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
        devices.clear();
    }
}
