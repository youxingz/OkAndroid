package com.cardioflex.pulse_generator.x;

import android.content.Context;
import android.util.Log;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.WebConfig;
import com.yanzhenjie.andserver.framework.website.AssetsWebsite;

import java.util.concurrent.TimeUnit;

public class XServer {
    private static final String TAG = "XServer";
    private Server server;

    public XServer(Context context) {
        this.server = AndServer.webServer(context)
                .port(8080)
                .timeout(100, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        Log.i(TAG, "xserver started");
                        Log.i(TAG, "Inet: " + server.getInetAddress().toString());
                        Log.i(TAG, "Port: " + server.getPort());
                    }

                    @Override
                    public void onStopped() {
                        Log.i(TAG, "xserver stopped");
                    }

                    @Override
                    public void onException(Exception e) {
                        Log.e(TAG, "xserver error");
                        e.printStackTrace();
                    }
                })
                .build();
    }

    public void start() {
        Log.i(TAG, "xserver start");
        if (!server.isRunning()) {
            server.startup();
        }
    }

    public void shutdown() {
        if (server != null && server.isRunning()) {
            Log.i(TAG, "xserver shutdown");
            server.shutdown();
        }
    }

    @Config
    public static class AppConfig implements WebConfig {

        @Override
        public void onConfig(Context context, Delegate delegate) {
            // 增加一个位于assets的web目录的网站
            delegate.addWebsite(new AssetsWebsite(context, "/out/"));
            // delegate.addWebsite(new FileBrowser("/web/"));
            // 增加一个位于/sdcard/Download/AndServer/目录的网站
            // delegate.addWebsite(new StorageWebsite(context, "/sdcard/Download/AndServer/"));
        }
    }
}
