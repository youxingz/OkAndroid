package io.okandroid.js;

import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

import io.okandroid.OkAndroid;
import io.okandroid.utils.GsonUtils;
import io.reactivex.rxjava3.core.Single;

public class OkWebView {
    private WebView webView;
    private static Map<String, OkWebView> webViewMap;

    public OkWebView(String name, WebView webView) {
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

    public Single<EventResponse> sendToWeb(Object data, boolean isByOkAndroid) {
        EventRequest request = new EventRequest(data, isByOkAndroid ? 1 : 0);
        String jsCode = "javascript:ok.onReceive(" + request.toJsonString() + ");";
        return Single.create(emitter -> {
            OkAndroid.mainThread().createWorker().schedule(() -> {
                try {
                    webView.evaluateJavascript(jsCode, returnValue -> {
                        // parse
                        EventResponse response = GsonUtils.getInstance().fromJson(returnValue, EventResponse.class);
                        emitter.onSuccess(response);
                    });
                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }
}
