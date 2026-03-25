package com.example.android_python;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Main_Camera extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        Button btn = findViewById(R.id.btn_batCamera);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(Main_Camera.this, CameraActivity.class);
            startActivity(intent);
        });
    }

}
