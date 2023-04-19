package com.cardioflex.pulse_generator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.cardioflex.pulse_generator.x.XServer;

import io.okandroid.bluetooth.le.OkBleClient;
import io.okandroid.cardioflex.pulsegen.Nordic52832;
import io.okandroid.js.OkWebView;
import io.reactivex.rxjava3.functions.Consumer;

public class DashboardActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private static final String URL = "http://localhost:8080/device_dashboard.html";
    // private static final String URL = "http://192.168.3.194:3000/device_dashboard";
    private static OkWebView okWebView;
    private static Nordic52832 nordic52832;
    private static OkBleClient.ConnectionStatus nrf52832ConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        requestPermissions();
        initServer();
        initWebView();
        initNordic52832();
    }

    public static OkWebView getOkWebViewInstance() {
        return okWebView;
    }

    public static Nordic52832 getNordic52832Instance() {
        return nordic52832;
    }

    public static OkBleClient.ConnectionStatus getNrf52832ConnectionStatus() {
        return nrf52832ConnectionStatus;
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.pg_webview);
        webView.setWebViewClient(new OkWebClient());
        webView.setWebChromeClient(new OkChromeClient());
        // webView.loadUrl("https://www.zhihu.com");
        webView.loadUrl(URL);
        webView.reload();
        //        webView.goBack();
        okWebView = new OkWebView("PG", webView);
    }

    private void initServer() {
        XServer server = new XServer(this);
        server.start();
    }

    @SuppressLint("CheckResult")
    private void initNordic52832() {
        nordic52832 = new Nordic52832(this, "F5:34:F4:78:DB:AA");
        nordic52832.connect().subscribe(connectionStatus -> {
            Log.i(TAG, "connection::" + connectionStatus.name());
            if (connectionStatus == OkBleClient.ConnectionStatus.connected) {
                // if (!notFirstConn) {
                // notFirstConn = true;
                // System.out.println(client.getBluetoothGatt().getServices().size());
                // startServiceJob();
                // }
            }
            nrf52832ConnectionStatus = connectionStatus;
        });
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            Log.i(TAG, "Internet Already Prepared");
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 2);
        }
    }

    /**
     * 权限申请返回结果
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 申请结果数组，里面都是int类型的数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) { //安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) { //这个是权限拒绝
                            String s = permissions[i];
                            Toast.makeText(this, s + "权限被拒绝", Toast.LENGTH_SHORT).show();
                        } else { //授权成功了
                            // do Something
                        }
                    }
                }
                break;
            case 2:
                // ble.
            default:
                break;
        }
    }
}