package com.cardioflex.bci;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<DeviceListAdapter.Model> {
    private List<Model> devices;
    private FullscreenActivity context;

    public DeviceListAdapter(@NonNull FullscreenActivity context, int resource) {
        super(context, resource);
        devices = new ArrayList<>();
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void addDevice(BluetoothDevice device) {
        for (int i = 0; i < devices.size(); i++) {
            Model device_ = devices.get(i);
            if (device_ == null) continue;
            if (device.getAddress().equals(device_.macAddress)) {
                return;
            }
        }
        Model model = new Model();
        model.isSampling = false;
        model.title = device.getName();
        model.macAddress = device.getAddress();
        devices.add(model);
        this.add(model);
    }

    public Model getDevice(int position) {
        return devices.get(position);
    }


    @SuppressLint("MissingPermission")
    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.device_item, null, false);
        }
        Model device = getDevice(position);
        TextView title = convertView.findViewById(R.id.device_name);
        title.setText(device.title);
        TextView address = convertView.findViewById(R.id.device_attr);
        address.setText("(" + device.macAddress + ")");
        Button connectBtn = convertView.findViewById(R.id.start_conn_btn);
        connectBtn.setOnClickListener(v -> {
            if (!device.isSampling) {
                context.worker.startConnDevice(device.macAddress);
                device.isSampling = true;
                connectBtn.setText("停止采样");
            } else {
                context.worker.stopSample(device.macAddress);
                device.isSampling = false;
                connectBtn.setText("开始采样");
            }
        });
        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
        devices.clear();
    }

    public class Model {
        String title;
        String macAddress;
        boolean isSampling;
    }
}
