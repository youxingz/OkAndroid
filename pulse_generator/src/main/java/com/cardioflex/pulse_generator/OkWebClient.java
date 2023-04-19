package com.cardioflex.pulse_generator;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OkWebClient extends WebViewClient {
    private static final String TAG = "OkWebClient";
    private static final String[] availableHosts = new String[]{
            "zhihu.com", "roumai.org", "localhost"
    };

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
}
