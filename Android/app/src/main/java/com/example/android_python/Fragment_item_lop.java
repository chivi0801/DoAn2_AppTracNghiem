package com.example.android_python;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Fragment_item_lop extends Fragment {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private String tenLop;
    private String examName;
    private int lopId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_lop, container, false);

        if (getArguments() != null) {
            tenLop = getArguments().getString("TEN_LOP");
            examName = getArguments().getString("EXAM_NAME");
        }

        setupToolbar();

        viewPager = view.findViewById(R.id.viewPager_item_lop);
        bottomNavigationView = view.findViewById(R.id.bottom_nav);

        // Khởi tạo Adapter cho ViewPager2 (Giữ nguyên như cũ)
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 1. Xử lý khi CLICK vào nút dưới cùng -> Đổi trang
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_baithi) {
                viewPager.setCurrentItem(0); // Chuyển sang Tab Bài Thi
                return true;
            } else if (itemId == R.id.nav_danhsach) {
                viewPager.setCurrentItem(1); // Chuyển sang Tab Danh Sách
                return true;
            }
            return false;
        });

        // 2. Xử lý khi VUỐT màn hình -> Cập nhật nút dưới cùng
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNavigationView.getMenu().findItem(R.id.nav_baithi).setChecked(true);
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.nav_danhsach).setChecked(true);
                }
            }
        });

        // Đảm bảo mặc định mở lên là ở Tab Bài Thi
        viewPager.setCurrentItem(0);

        return view;
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            TextView title = activity.findViewById(R.id.toolbar_title);
            if (title != null) {
                if (examName != null && !examName.isEmpty()) {
                    title.setText(examName + " - " + tenLop);
                } else {
                    title.setText(tenLop);
                }
            }

            //nút quay về trang trước
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> {
                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                });
            }

        }


    }
    private class MyViewPagerAdapter extends FragmentStateAdapter {
        public MyViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Trong hàm createFragment của MyViewPagerAdapter
            if (position == 1) {
                Fragment_DanhSachLop fragmentDanhSach = new Fragment_DanhSachLop();
                Bundle bundle = new Bundle();
                bundle.putInt("LOP_ID", lopId);
                fragmentDanhSach.setArguments(bundle);
                return fragmentDanhSach;
            }
            return new Fragment_BaiThi();
        }


        @Override
        public int getItemCount() {
            return 2;
        }
    }
}