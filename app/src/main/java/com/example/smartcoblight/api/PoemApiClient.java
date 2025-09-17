package com.example.smartcoblight.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PoemApiClient {

    private final PoemReqListener listener;

    public PoemApiClient(PoemReqListener listener) {
        this.listener = listener;
    }

    public void req() {

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().get().url("http://121.36.251.16:8000/api/query").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onPoemFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string(); // 获取响应体
                    listener.onPoemSuccess(responseData);
                } else {
                    listener.onPoemFailure();
                }
            }
        });
    }

    public interface PoemReqListener {
        void onPoemFailure();

        void onPoemSuccess(String content);
    }
}
