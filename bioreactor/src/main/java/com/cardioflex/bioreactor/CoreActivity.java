package com.cardioflex.bioreactor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cardioflex.bioreactor.motor.PulseMotor;
import com.cardioflex.bioreactor.motor.PulseMotorWorker;
import com.cardioflex.bioreactor.sys.Sys;
import com.cardioflex.bioreactor.x.XServer;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.okandroid.js.OkWebView;
import io.okandroid.usb.USB;

public class CoreActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private static final String URL = "http://localhost:8080/index.html";
    private static OkWebView okWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        requestPermissions();
        initServer();
        initWebView();
        initUSB();
        startWorking();

        // test
        // USB usb = new USB(this);
        // try {
        //     usb.open(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        //     Thread.sleep(2000);
        //     String pay_str = "{\"config\":{\"items\":[{\"direction\":false,\"time\":0,\"velocity\":0},{\"direction\":false,\"time\":0,\"velocity\":0}],\"turn\":false},\"motorId\":1,\"req\":1970480664}";
        //     byte[] pay_bytes = pay_str.getBytes(StandardCharsets.UTF_8);
        //     byte[] request_bytes = new byte[2 + pay_bytes.length];
        //     System.arraycopy(pay_bytes, 0, request_bytes, 2, pay_bytes.length);
        //     request_bytes[0] = (byte) 0x02;
        //     request_bytes[1] = (byte) 0x02;
        //     usb.write(request_bytes);
        //     Thread.sleep(2000);
        //     while (true) {
        //         byte[] data = usb.read();
        //         if (data.length == 1024) {
        //             System.out.println("??????????????????????????????????");
        //         }
        //         System.out.print(new String(data, Charset.defaultCharset()));
        //     }
        // } catch (Exception e) {
        //     throw new RuntimeException(e);
        // }
    }

    public static OkWebView getOkWebViewInstance() {
        return okWebView;
    }

    private void initUSB() {
        try {
            PulseMotor.setCoreActivityContext(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.pg_webview);
        OkWebClient client = new OkWebClient(this);
        webView.setWebViewClient(client);
        webView.setWebChromeClient(new OkChromeClient());
        // webView.loadUrl("https://www.zhihu.com");
        webView.loadUrl(URL);
        webView.reload();
        //        webView.goBack();
        okWebView = new OkWebView("PG", webView);
        client.setOkWebView(okWebView);
    }

    private void initServer() {
        XServer server = new XServer(this);
        server.start();
    }

    private void startWorking() {
    }


    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            Log.i(TAG, "Internet Already Prepared");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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