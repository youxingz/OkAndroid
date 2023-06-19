package com.cardioflex.motor.gmt;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cardioflex.motor.R;
import com.cardioflex.motor.leadfluid_x.LiquidActivity;

public class GMTCellAdapter extends ArrayAdapter<GMTCellAdapter.ConfigModel> {

    private Activity context;
    private GMTCell.OnDoneClick onDoneClick;

    public GMTCellAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
    }

    public void setOnDoneClick(GMTCell.OnDoneClick onDoneClick) {
        this.onDoneClick = onDoneClick;
    }

    public ConfigModel getModel(int index) {
        return super.getItem(index);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.view_gmt_list_item, null, false);
        }
        ConfigModel model = getItem(position);
        ((TextView) convertView.findViewById(R.id.velocity_text)).setText(model.velocity + " rpm");
        ((TextView) convertView.findViewById(R.id.time_text)).setText(model.seconds + " ms");
        ((TextView) convertView.findViewById(R.id.direction_text)).setText(model.isClockwise ? "顺时针🔁" : "逆时针🔄");
        convertView.findViewById(R.id.delete_btn).setOnClickListener(v -> {
            // delete self.
            GMTCellAdapter.this.remove(model);
            GMTCellAdapter.this.notifyDataSetChanged();
            System.out.println("Config Remove.");
            if (onDoneClick != null) {
                onDoneClick.onDone();
            }
        });
        convertView.findViewById(R.id.container).setOnClickListener(v -> {
            System.out.println(position);
            // LiquidActivity.currentModel = model;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.view_gmt_dialog, null);
            RadioButton directionL = dialogView.findViewById(R.id.direction_option_l);
            RadioButton directionR = dialogView.findViewById(R.id.direction_option_r);
            EditText velocityEdit = dialogView.findViewById(R.id.velocity_edit);
            EditText timeEdit = dialogView.findViewById(R.id.time_edit);
            // init view
            directionL.setChecked(model.isClockwise);
            directionR.setChecked(!model.isClockwise);
            velocityEdit.setText(model.velocity + "");
            timeEdit.setText(model.seconds + "");
            builder.setView(dialogView);
            builder.setTitle("配置编辑");
            builder.setPositiveButton("完成", (dialog, which) -> {
                String velocityStr = velocityEdit.getText().toString();
                String timeStr = timeEdit.getText().toString();
                boolean isL = directionL.isChecked();
                boolean isR = directionR.isChecked();
                boolean direction = isL && !isR; // 逆时针
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
                model.isClockwise = direction;
                GMTCellAdapter.this.notifyDataSetChanged();
                if (onDoneClick != null) {
                    onDoneClick.onDone();
                }
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
        public Double velocity;
        public Integer seconds;
        public boolean isClockwise;

        public ConfigModel(Double velocity, Integer seconds, boolean isClockwise) {
            this.velocity = velocity;
            this.seconds = seconds;
            this.isClockwise = isClockwise;
        }
    }
}
