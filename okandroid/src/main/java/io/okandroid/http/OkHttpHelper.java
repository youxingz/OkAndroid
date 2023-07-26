package io.okandroid.http;

import java.io.IOException;

import io.okandroid.utils.GsonUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHelper {
    private static OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static Response get(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            return new OkHttpClient().newCall(request).execute();
            // return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
            return null;
        }
    }

    public static Response post(String url, Object data) {
        RequestBody body = RequestBody.create(GsonUtils.getInstance().toJson(data), JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            return new OkHttpClient().newCall(request).execute();
            // return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            // throw new RuntimeException(e);
            return null;
        }
    }
}
