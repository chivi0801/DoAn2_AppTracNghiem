package com.example.android_python;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Cài đặt Toolbar (Chỉ gọi 1 lần ở Activity)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Nạp Fragment đầu tiên (CHỈ NẠP 1 LẦN)
        if (savedInstanceState == null) {
            loadFragment(new Fragment_DsKyThi());
        }
    }

    /**
     * Hàm dùng để chuyển đổi giữa các Fragment (Ví dụ: từ Danh sách sang Quét ảnh)
     */
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}