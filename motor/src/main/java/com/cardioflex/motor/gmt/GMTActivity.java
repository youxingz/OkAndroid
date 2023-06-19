package com.cardioflex.motor.gmt;

import android.os.Bundle;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.cardioflex.motor.R;

import java.util.ArrayList;
import java.util.List;

public class GMTActivity extends AppCompatActivity {
    GridLayout gridLayout;

    private List<IdConfig> configs = new ArrayList<>();

    {
        configs.add(new IdConfig("10.168.1.20", 1));
        configs.add(new IdConfig("10.168.1.20", 2));
        configs.add(new IdConfig("10.168.1.20", 3));
        configs.add(new IdConfig("10.168.1.20", 4));
        configs.add(new IdConfig("10.168.1.21", 1));
        configs.add(new IdConfig("10.168.1.21", 2));
        configs.add(new IdConfig("10.168.1.21", 3));
        configs.add(new IdConfig("10.168.1.21", 4));
        configs.add(new IdConfig("10.168.1.22", 1));
        configs.add(new IdConfig("10.168.1.22", 2));
        configs.add(new IdConfig("10.168.1.22", 3));
        configs.add(new IdConfig("10.168.1.22", 4));
        configs.add(new IdConfig("10.168.1.23", 1));
        configs.add(new IdConfig("10.168.1.23", 2));
        configs.add(new IdConfig("10.168.1.23", 3));
        configs.add(new IdConfig("10.168.1.23", 4));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmt);
        initView();
    }

    private void initView() {
        gridLayout = findViewById(R.id.container);
        for (int i = 0; i < 14; i++) {
            IdConfig config = configs.get(i);
            GMTCell cell = new GMTCell(this, i + 1, config.getIp(), config.getMotorId());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(i % 5);
            params.rowSpec = GridLayout.spec(i / 5);
            gridLayout.addView(cell, params);
        }
    }

    public static class IdConfig {
        private String ip;
        private int motorId;

        public IdConfig(String ip, int motorId) {
            this.ip = ip;
            this.motorId = motorId;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getMotorId() {
            return motorId;
        }

        public void setMotorId(int motorId) {
            this.motorId = motorId;
        }
    }
}