package com.example.android_python;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Activity_DangKy extends AppCompatActivity {

    EditText edtTenTaiKhoan, edtHoTen, edtMatKhau, edtXacNhanMatKhau;
    Button btnDoRegister;
    TaoCSDL dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        // 2. Ánh xạ giao diện (NHỚ ĐỔI ID CHO KHỚP VỚI FILE XML CỦA BẠN NHÉ)
        edtTenTaiKhoan = findViewById(R.id.edtTenTaiKhoan);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtXacNhanMatKhau = findViewById(R.id.edtXacNhanMatKhau);
        btnDoRegister = findViewById(R.id.btnDoRegister);

        dbHelper = new TaoCSDL(this);

        // 3. Xử lý logic đăng ký
        btnDoRegister.setOnClickListener(v -> {

            String tenTaiKhoan = edtTenTaiKhoan.getText().toString().trim();
            String hoTen = edtHoTen.getText().toString().trim();
            String matKhau = edtMatKhau.getText().toString().trim();
            String xacNhanMatKhau = edtXacNhanMatKhau.getText().toString().trim();

            if (tenTaiKhoan.isEmpty() || hoTen.isEmpty() || matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại
            }

            if (!matKhau.equals(xacNhanMatKhau)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại
            }

            boolean IsInserted = dbHelper.themGiangVien(tenTaiKhoan, hoTen, matKhau);

            if (IsInserted) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay về màn hình Login
            } else {
                Toast.makeText(this, "Tên tài khoản này đã có người sử dụng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}