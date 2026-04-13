package com.example.android_python;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class Fragment_DanhSachLop extends Fragment {

    private RecyclerView rvDanhSachThiSinh;
    private FloatingActionButton fabAddThiSinh;

    private ThiSinhAdapter thiSinhAdapter;
    private ArrayList<ThiSinh> danhSachThiSinh;
    private TaoCSDL dbHelper;

    private int currentLopID = -1; // Biến lưu ID của lớp hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danhsachlop, container, false);

        // 1. NHẬN ID LỚP TỪ FRAGMENT CHA TRUYỀN SANG
        if (getArguments() != null) {
            currentLopID = getArguments().getInt("LOP_ID", -1);
        }

        // 2. ÁNH XẠ VIEW & KHỞI TẠO CƠ SỞ DỮ LIỆU
        dbHelper = new TaoCSDL(requireContext());
        rvDanhSachThiSinh = view.findViewById(R.id.rvDanhSachThiSinh);
        fabAddThiSinh = view.findViewById(R.id.fabAddThiSinh);

        rvDanhSachThiSinh.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 3. LOAD DỮ LIỆU TỪ SQLITE LÊN RECYCLERVIEW
        loadDanhSachThiSinh();

        // 4. BẮT SỰ KIỆN NÚT THÊM
        fabAddThiSinh.setOnClickListener(v -> {
            if (currentLopID != -1) {
                showDialogThemThiSinh();
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không xác định được Lớp", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadDanhSachThiSinh() {
        // Lấy danh sách từ CSDL dựa vào ID Lớp
        danhSachThiSinh = dbHelper.layDanhSachThiSinhTheoLop(currentLopID);

        // Cài đặt Adapter
        thiSinhAdapter = new ThiSinhAdapter(danhSachThiSinh);
        rvDanhSachThiSinh.setAdapter(thiSinhAdapter);
    }

    private void showDialogThemThiSinh() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_them_thi_sinh);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText edtThiSinhID = dialog.findViewById(R.id.edt_ThiSinh_ID);
        EditText edtHoTen = dialog.findViewById(R.id.edt_HoTen);
        Button btnHuy = dialog.findViewById(R.id.btn_Huy);
        Button btnLuu = dialog.findViewById(R.id.btn_Luu);

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        btnLuu.setOnClickListener(v -> {
            String maThiSinh = edtThiSinhID.getText().toString().trim();
            String hoTen = edtHoTen.getText().toString().trim();

            if (maThiSinh.isEmpty() || hoTen.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thực hiện thêm vào Database
            long result = themThiSinhVaoDB(maThiSinh, currentLopID, hoTen);

            if (result != -1) {
                // Thêm thành công -> Cập nhật lại giao diện ngay lập tức
                danhSachThiSinh.add(new ThiSinh(maThiSinh, currentLopID, hoTen));
                thiSinhAdapter.notifyItemInserted(danhSachThiSinh.size() - 1);
                // Cuộn xuống cuối danh sách để thấy người vừa thêm
                rvDanhSachThiSinh.scrollToPosition(danhSachThiSinh.size() - 1);

                Toast.makeText(requireContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Lỗi! Mã thí sinh có thể đã tồn tại.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private long themThiSinhVaoDB(String thiSinhId, int lopId, String hoTen) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("ThiSinh_ID", thiSinhId);
        values.put("Lop_ID", lopId);
        values.put("HoTen", hoTen);

        return db.insert("ThiSinh", null, values);
    }
}