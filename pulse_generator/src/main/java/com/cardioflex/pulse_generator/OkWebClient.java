package com.cardioflex.pulse_generator;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.okandroid.js.OkWebView;

public class OkWebClient extends WebViewClient {
    private static final String TAG = "OkWebClient";
    private static final String[] availableHosts = new String[]{
            "zhihu.com", "roumai.org", "localhost"
    };

    private CoreActivity coreActivity;

    public OkWebClient(CoreActivity coreActivity) {
        this.coreActivity = coreActivity;
    }

    private OkWebView okWebView;

    public void setOkWebView(OkWebView okWebView) {
        this.okWebView = okWebView;
    }

    /**
     * @param view
     * @param request
     * @return {@code true} to cancel the current load, otherwise return {@code false}.
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        //        return false;
        // Uri uri = request.getUrl();
        // Log.i(TAG, uri.toString());
        // Log.i(TAG, uri.getScheme());
        // System.out.println(uri);
        // if (!uri.getScheme().startsWith("http")) {
        //     return true;
        // }
        // Log.i(TAG, uri.getHost());
        // String host = uri.getHost();
        // for (String gate : availableHosts) {
        //     if (host.endsWith(gate)) {
        //         return false;
        //     }
        // }
        // return true;
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (okWebView != null) {
            okWebView.setPageLoading(true);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (okWebView != null) {
            okWebView.setPageLoading(false);
        }
        Uri uri = Uri.parse(url);
        System.out.println(">>>>>url:  " + url);
        System.out.println(">>>>>path: " + uri.getPath());
        switch (uri.getPath()) {
            case "/device_list.html": {
                PageDeviceList.setCoreActivity(coreActivity);
                PageDeviceList.startScanDevice(); // 进入页面自动开始设备搜索
                break;
            }
            case "/device_dashboard.html": {
                PageDashboard.setCoreActivity(coreActivity);
                PageDeviceList.stopScanDevice();
                String macAddress = uri.getQueryParameter("mac");
                Log.i(TAG, "ConnectTo: [device mac] " + macAddress);
                PageDashboard.startConnect(macAddress); // 进入页面自动开始设备连接
                break;
            }
        }
    }
}
