package com.cardioflex.motor.leadfluid_x;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cardioflex.motor.R;

public class ConfigListAdapter extends ArrayAdapter<ConfigListAdapter.ConfigModel> {

    private Activity context;

    public ConfigListAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.liquid_list_item, null, false);
        }
        ConfigModel model = getItem(position);
        ((TextView) convertView.findViewById(R.id.pump_title)).setText(model.pumpName);
        ((TextView) convertView.findViewById(R.id.velocity_text)).setText(model.velocity + " rpm");
        ((TextView) convertView.findViewById(R.id.time_text)).setText(model.seconds + " min");
        ((TextView) convertView.findViewById(R.id.direction_text)).setText(model.isClockwise ? "顺时针🔁" : "逆时针🔄");
        convertView.findViewById(R.id.delete_btn).setOnClickListener(v -> {
            // delete self.
            ConfigListAdapter.this.remove(model);
            ConfigListAdapter.this.notifyDataSetChanged();
        });
        convertView.findViewById(R.id.container).setOnClickListener(v -> {
            System.out.println(position);
            LiquidActivity.currentModel = model;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.liquid_dialog, null);
            EditText velocityEdit = dialogView.findViewById(R.id.velocity_edit);
            EditText timeEdit = dialogView.findViewById(R.id.time_edit);
            builder.setView(dialogView);
            builder.setTitle("配置编辑");
            builder.setPositiveButton("完成", (dialog, which) -> {
                String velocityStr = velocityEdit.getText().toString();
                String timeStr = timeEdit.getText().toString();
                double velocity = 0.0;
                int time = 0;
                try {
                    velocity = Double.parseDouble(velocityStr);
                    time = Integer.parseInt(timeStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                model.velocity = velocity;
                model.seconds = time;
                ConfigListAdapter.this.notifyDataSetChanged();
            });
            builder.setCancelable(false);
            builder.show();
            // dialogView.findViewById(R.id.pump_selection).requestFocus();
            velocityEdit.clearFocus();
            timeEdit.clearFocus();
            dialogView.clearFocus();
        });
        return convertView;
    }

    public static class ConfigModel {
        public String pumpName;
        public Double velocity;
        public Integer seconds;
        public boolean isClockwise;

        public ConfigModel(String pumpName, Double velocity, Integer seconds, boolean isClockwise) {
            this.pumpName = pumpName;
            this.velocity = velocity;
            this.seconds = seconds;
            this.isClockwise = isClockwise;
        }
    }
}
