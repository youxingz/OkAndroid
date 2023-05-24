package com.cardioflex.pulse_generator;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class OkChromeClient extends WebChromeClient {
    // var result=prompt("js://demo?arg1=111&arg2=222");
    //
    // 拦截输入框(原理同方式shouldOverrideUrlLoading)
    // 参数message:代表promt())的内容（不是url）
    // 参数result:代表输入框的返回值
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // try {
        // System.out.println(consoleMessage.message());
        // System.out.println(consoleMessage.messageLevel());
        Log.i("Console", consoleMessage.message());
        //     Thread.sleep(5000);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        return false;
        // return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        new Thread(() -> {
            try {
                System.out.println(url);
                System.out.println(message);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.confirm("Android回调给JS的数据为useid=123456");
        }).start();
        return true;
        //
        //        // 步骤2：根据协议的参数，判断是否是所需要的url(原理同方式2)
        //        // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
        //        //传入进来的 url="js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
        //        Uri uri = Uri.parse(message);
        //        // 如果url的协议 = 预先约定的 js 协议,就解析往下解析参数
        //        if (uri.getScheme().equals("js")) {
        //            // 如果 authority = 预先约定协议里的webview，即代表符合约定的协议
        //            // 所以拦截url,下面JS开始调用Android需要的方法
        //            if (uri.getAuthority().equals("webview")) {
        //
        //                // 步骤3：执行JS所需要调用的逻辑
        //                System.out.println("js调用了Android的方法");
        //                // 可以在协议上带有参数并传递到Android上
        //                HashMap<String, String> params = new HashMap<>();
        //                Set<String> collection = uri.getQueryParameterNames();
        //
        //                //参数result:代表消息框的返回值(输入值)
        //                result.confirm("Android回调给JS的数据为useid=123456");
        //            }
        //            return true;
        //        }
        // return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    // 拦截JS的警告框
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    // 拦截JS的确认框
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

}
