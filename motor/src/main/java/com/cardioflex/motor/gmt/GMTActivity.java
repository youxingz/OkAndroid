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
        configs.add(new IdConfig("10.168.1.20", 1, "A1"));
        configs.add(new IdConfig("10.168.1.20", 2, "A2"));
        configs.add(new IdConfig("10.168.1.20", 3, "A3"));
        configs.add(new IdConfig("10.168.1.20", 4, "A4"));
        configs.add(new IdConfig("10.168.1.21", 1, "B1"));
        configs.add(new IdConfig("10.168.1.21", 2, "B2"));
        configs.add(new IdConfig("10.168.1.21", 3, "B3"));
        configs.add(new IdConfig("10.168.1.21", 4, "B4"));
        configs.add(new IdConfig("10.168.1.22", 1, "C1"));
        configs.add(new IdConfig("10.168.1.22", 2, "C2"));
        configs.add(new IdConfig("10.168.1.22", 3, "C3"));
        configs.add(new IdConfig("10.168.1.22", 4, "C4"));
        configs.add(new IdConfig("10.168.1.23", 1, "D1"));
        configs.add(new IdConfig("10.168.1.23", 2, "D2"));
        configs.add(new IdConfig("10.168.1.23", 3, "D3"));
        configs.add(new IdConfig("10.168.1.23", 4, "D4"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmt);
        initView();
    }

    private void initView() {
        gridLayout = findViewById(R.id.grid_container);
        int col = 4;
        for (int i = 0; i < 16; i++) {
            IdConfig config = configs.get(i);
            GMTCell cell = new GMTCell(this, i + 1, config.getIp(), config.getMotorId(), config.getName());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(i % col);
            params.rowSpec = GridLayout.spec(i / col);
            gridLayout.addView(cell, params);
        }
    }

    public static class IdConfig {
        private String ip;
        private int motorId;

        private String name;

        public IdConfig(String ip, int motorId, String name) {
            this.ip = ip;
            this.motorId = motorId;
            this.name = name;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}