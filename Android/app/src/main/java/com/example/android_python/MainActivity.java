package com.example.android_python;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 1. Cài đặt Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Bắt sự kiện nút Menu để mở Sidebar
        ImageView iconMenu = findViewById(R.id.icon_Menu);
        iconMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 3. Xử lý khi nhấn vào các mục trong Sidebar
        setupDrawerContent(navigationView);

        // 4. Hiển thị thông tin giảng viên trên Header Sidebar
        updateNavHeader();

        // 5. Nạp Fragment đầu tiên
        if (savedInstanceState == null) {
            loadFragment(new Fragment_DsKyThi());
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId(); // Biến id được khai báo ở đây

            if (id == R.id.nav_exams) {
                loadFragment(new Fragment_DsKyThi());
            }
            else if (id == R.id.nav_classes) {
                // SỬA TẠI ĐÂY: Thay cái Toast bằng lệnh nạp Fragment_Lop
                loadFragment(new Fragment_Lop());
            }
            else if (id == R.id.nav_logout) {
                // ... code logout giữ nguyên ...
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
                finish();
                Intent intent = new Intent(this, Activity_DangNhap.class);
                startActivity(intent);
            }

            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tv_username);
        TextView tvUserId = headerView.findViewById(R.id.tv_user_id);

        android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String hoTen = sharedPreferences.getString("HO_TEN", "Giảng Viên");
        int gvId = sharedPreferences.getInt("GV_ID", -1);

        tvUsername.setText(hoTen);
        tvUserId.setText("ID: " + (gvId != -1 ? gvId : "N/A"));
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