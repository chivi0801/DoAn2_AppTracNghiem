package com.example.android_python;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnDoRegister = findViewById(R.id.btnDoRegister);

        btnDoRegister.setOnClickListener(v -> {
            // Hiển thị thông báo
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

            // Tự động quay về trang đăng nhập
            finish();
        });
    }
}