package com.example.android_python;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {

    TextView resultText;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnSend);
        resultText = findViewById(R.id.resultText);

        btn.setOnClickListener(v -> sendImage());
    }

    void sendImage(){

        Request request = new Request.Builder()
                .url("http://192.168.1.37:5000/scan")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        resultText.setText(e.toString())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();

                runOnUiThread(() ->
                        resultText.setText(res)
                );
            }
        });
    }

}