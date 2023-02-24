package com.cardioflex.motor;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<String> {
    private List<File> devices;

    public DeviceListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        devices = new ArrayList<>();
    }

    public void addDevice(File device) {
        devices.add(device);
        this.add(device.getName());
    }

    public File getDevice(int position) {
        return devices.get(position);
    }

    @Override
    public void clear() {
        super.clear();
        devices.clear();
    }
}
