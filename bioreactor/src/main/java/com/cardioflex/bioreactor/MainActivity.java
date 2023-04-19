package com.cardioflex.bioreactor;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.okandroid.OkAndroid;
import io.okandroid.opcua.OpcClient;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button button;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.log_text);
        button = findViewById(R.id.test_btn);
        button.setOnClickListener(view -> {
            textView.setText("Done!  000000");
            try {
                OkAndroid.newThread().scheduleDirect(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        run();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                textView.setText(e.getLocalizedMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void run() {
        OkAndroid.mainThread().createWorker().schedule(() -> {
            textView.setText("Done!  0");
        });
        new OpcClient((client, future) -> {
            try {
                OkAndroid.mainThread().createWorker().schedule(() -> {
                    textView.setText("Done!  1");
                });
                System.out.println("Hello!");
                UaClient uaClient = client.connect().get();
                List<ReferenceDescription> descriptions = uaClient.getAddressSpace().browse(NodeId.parse(""));
                System.out.println(descriptions.size());
                future.complete(client);
                OkAndroid.mainThread().createWorker().schedule(() -> {
                    textView.setText("Done!  2");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}