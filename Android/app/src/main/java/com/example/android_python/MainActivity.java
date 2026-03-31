package com.example.android_python;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kết nối với file XML có FragmentContainerView mà bạn vừa khoe lúc nãy
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ExamListFragment())
                    .commit();
        }

        // 1. Cài đặt Toolbar (Thanh tiêu đề màu xanh)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // QUAN TRỌNG: Tắt tiêu đề mặc định của hệ thống
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Nạp Fragment đầu tiên (Trang danh sách kỳ thi) vào khung chứa
        if (savedInstanceState == null) {
            loadFragment(new ExamListFragment());
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