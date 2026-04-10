package com.example.android_python;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Activity_DangNhap extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    TaoCSDL dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        // Ánh xạ View
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        dbHelper = new TaoCSDL(this);


        // 3. XỬ LÝ SỰ KIỆN KHI BẤM VÀO CHỮ "ĐĂNG KÝ"
        tvRegister.setOnClickListener(v -> {
            // Lệnh chuyển từ Login sang Register
            Intent intent = new Intent(Activity_DangNhap.this, Activity_DangKy.class);
            startActivity(intent);
        });
        btnLogin.setOnClickListener(v -> {
            String user = edtUsername.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm kiểm tra từ SQLite
            int gvId = dbHelper.KiemTraDangNhap(user, pass);

            if (gvId != -1) { // Nếu tìm thấy (gvId khác -1)
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("GV_ID", gvId); // Lưu GV_ID lại
                editor.apply(); // Xác nhận lưu

                // Chuyển sang màn hình chính (MainActivity)
                Intent intent = new Intent(Activity_DangNhap.this, MainActivity.class);
                startActivity(intent);
                finish(); // Đóng luôn màn hình Login để người dùng ấn nút "Back" trên điện thoại không bị quay lại đây nữa
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}