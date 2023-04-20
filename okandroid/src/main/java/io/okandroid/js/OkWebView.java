package io.okandroid.js;

import android.annotation.SuppressLint;
import android.webkit.WebView;

import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;

import io.okandroid.OkAndroid;
import io.okandroid.exception.OkAndroidException;
import io.okandroid.utils.GsonUtils;
import io.reactivex.rxjava3.core.Single;

public class OkWebView {
    private WebView webView;
    private static Map<String, OkWebView> webViewMap;
    private volatile boolean isPageLoading = true;

    @SuppressLint("SetJavaScriptEnabled")
    public OkWebView(String name, WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        this.webView = webView;
        if (webViewMap == null) {
            webViewMap = new HashMap<>();
        }
        webViewMap.put(name, this);
    }

    public static OkWebView getInstance(String name) {
        if (webViewMap == null) return null;
        return webViewMap.get(name);
    }

    public boolean isPageLoading() {
        return isPageLoading;
    }

    public void setPageLoading(boolean isPageLoading) {
        this.isPageLoading = isPageLoading;
    }

    public Single<EventResponse> sendToWeb(Object data) {
        return sendToWeb(data, false);
    }

    public Single<EventResponse> sendToWeb(Object data, boolean isByOkAndroid) {
        EventRequest request = new EventRequest(data, isByOkAndroid ? 1 : 0);
        String jsCode = "javascript:window.$ok.onReceive(" + request.toJsonString() + ");";
        // System.out.println(">>>>>>>>" + jsCode);
        return Single.create(emitter -> {
            OkAndroid.mainThread().createWorker().schedule(() -> {
                if (isPageLoading()) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(new OkAndroidException("Page Loading.., Please Wait."));
                    }
                    return;
                }
                try {
                    webView.evaluateJavascript(jsCode, returnValue -> {
                        try {
                            // System.out.println(">>>>>>>" + returnValue);
                            // parse
                            EventResponse response = GsonUtils.getInstance().fromJson(returnValue, EventResponse.class);
                            if (!emitter.isDisposed()) {
                                emitter.onSuccess(response);
                            }
                        } catch (JsonSyntaxException e) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }
            });
        });
    }
}
