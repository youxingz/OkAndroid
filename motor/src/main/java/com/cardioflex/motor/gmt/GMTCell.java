package com.cardioflex.motor.gmt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.cardioflex.motor.R;

import java.util.ArrayList;
import java.util.List;

import io.okandroid.http.OkHttpHelper;
import okhttp3.Response;

public class GMTCell extends LinearLayout {
    private Activity activity;
    View root;
    private int id;
    private String ip;
    private int motorId;

    public GMTCell(Activity context, int id, String ip, int motorId) {
        super(context);
        this.activity = context;
        this.id = id;
        this.ip = ip;
        this.motorId = motorId;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        root = inflater.inflate(R.layout.view_gmt_cell, this);
        initView();
    }

    private void initView() {
        TextView titleView = findViewById(R.id.title_text);
        titleView.setText("脉搏【" + id + "】号【" + ip + "/" + motorId + "】");
        ToggleButton stopOrStart = findViewById(R.id.turn_btn);
        ListView listView = findViewById(R.id.config_list);
        GMTCellAdapter adapter = new GMTCellAdapter(activity, R.layout.view_gmt_list_item);
        adapter.setOnDoneClick(() -> {
            // http...
            System.out.println("Config Update.");
            sendHttp(adapter, stopOrStart.isChecked());
        });
        listView.setAdapter(adapter);
        Button button = findViewById(R.id.config_item_add_btn);
        button.setOnClickListener(v -> {
            adapter.add(new GMTCellAdapter.ConfigModel(100.0, 100, false));
            adapter.notifyDataSetChanged();
            // http...
            System.out.println("Config Add.");
            sendHttp(adapter, stopOrStart.isChecked());
        });
        stopOrStart.setOnClickListener(v -> {
            System.out.println("Start/Stop");
            // http..
            sendHttp(adapter, stopOrStart.isChecked());
        });
    }

    private void sendHttp(GMTCellAdapter adapter, boolean turnOn) {
        System.out.println("GMT Driver: HTTP/Request [turn: " + turnOn + "]");
        List<PulseMotorHttpConfigItem> items = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            GMTCellAdapter.ConfigModel model = adapter.getModel(i);
            items.add(new PulseMotorHttpConfigItem(model.velocity.intValue(), model.seconds, model.isClockwise));
        }
        PulseMotorHttpConfig payloadConfig = new PulseMotorHttpConfig(items, turnOn);
        PulseMotorHttpPayload payload = new PulseMotorHttpPayload(payloadConfig, motorId);
        System.out.println(payload);
        Response response = OkHttpHelper.post("http://" + ip + "/api/v1/config", payload);
        if (response == null || !response.isSuccessful()) {
            // set success.
            // throw new Exception("Http connect error.");
        }
        try {
            response.body().close();
        } catch (Exception e) {
            // throw new Exception("Http response body close error.");
        }
    }

    public interface OnDoneClick {
        void onDone();
    }

    public static class PulseMotorHttpPayload {
        private PulseMotorHttpConfig config;
        private int motorId;

        public PulseMotorHttpPayload(PulseMotorHttpConfig config, int motorId) {
            this.config = config;
            this.motorId = motorId;
        }

        public PulseMotorHttpConfig getConfig() {
            return config;
        }

        public void setConfig(PulseMotorHttpConfig config) {
            this.config = config;
        }

        public int getMotorId() {
            return motorId;
        }

        public void setMotorId(int motorId) {
            this.motorId = motorId;
        }

        @Override
        public String toString() {
            return "PulseMotorHttpPayload{" +
                    "config=" + config +
                    ", motorId=" + motorId +
                    '}';
        }
    }

    public static class PulseMotorHttpConfig {
        private List<PulseMotorHttpConfigItem> items;
        private boolean turn;

        public PulseMotorHttpConfig(List<PulseMotorHttpConfigItem> items, boolean turn) {
            this.items = items;
            this.turn = turn;
        }

        public List<PulseMotorHttpConfigItem> getItems() {
            return items;
        }

        public void setItems(List<PulseMotorHttpConfigItem> items) {
            this.items = items;
        }

        public boolean getTurn() {
            return turn;
        }

        public void setTurn(boolean turn) {
            this.turn = turn;
        }

        @Override
        public String toString() {
            return "PulseMotorHttpConfig{" +
                    "items=" + items +
                    ", turn=" + turn +
                    '}';
        }
    }

    public static class PulseMotorHttpConfigItem {
        private int velocity;
        private int time;
        private boolean direction;

        public PulseMotorHttpConfigItem(int velocity, int time, boolean direction) {
            this.velocity = velocity;
            this.time = time;
            this.direction = direction;
        }

        public int getVelocity() {
            return velocity;
        }

        public void setVelocity(int velocity) {
            this.velocity = velocity;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public boolean getDirection() {
            return direction;
        }

        public void setDirection(boolean direction) {
            this.direction = direction;
        }

        @Override
        public String toString() {
            return "PulseMotorHttpConfigItem{" +
                    "velocity=" + velocity +
                    ", time=" + time +
                    ", direction=" + direction +
                    '}';
        }
    }
}
