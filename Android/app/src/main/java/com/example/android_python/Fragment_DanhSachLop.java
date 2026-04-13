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
import androidx.appcompat.widget.PopupMenu;
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
        danhSachThiSinh = dbHelper.layDanhSachThiSinhTheoLop(currentLopID);
        thiSinhAdapter = new ThiSinhAdapter(danhSachThiSinh);

        // GỌI SỰ KIỆN CLICK Ở ĐÂY
        thiSinhAdapter.setOnItemClickListener(new ThiSinhAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ThiSinh thiSinh) {
                // Khi nhấn vào cả 1 dòng thí sinh
                Toast.makeText(requireContext(), "Bạn chọn: " + thiSinh.getHoTen(), Toast.LENGTH_SHORT).show();
                // Code chuyển sang xem chi tiết điểm số của thí sinh này (nếu có) bạn viết ở đây
            }

            @Override
            public void onOptionsClick(ThiSinh thiSinh, View view, int position) {
                // Khi nhấn vào nút 3 chấm -> Mở menu nhỏ
                showPopupMenu(view, thiSinh, position);
            }
        });

        rvDanhSachThiSinh.setAdapter(thiSinhAdapter);
    }

    // HÀM HIỂN THỊ MENU 3 CHẤM
    private void showPopupMenu(View view, ThiSinh thiSinh, int position) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenu().add("Chỉnh sửa");
        popupMenu.getMenu().add("Xóa");

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Chỉnh sửa")) {

                // GỌI HÀM SỬA Ở ĐÂY NÀY
                showDialogSuaThiSinh(thiSinh, position);

                return true;
            } else if (item.getTitle().equals("Xóa")) {
                // Xóa khỏi danh sách hiển thị
                danhSachThiSinh.remove(position);
                thiSinhAdapter.notifyItemRemoved(position);

                // Xóa thật trong Database (Bạn tự viết thêm hàm xoaThiSinh(id) trong dbHelper nhé)
                // dbHelper.xoaThiSinh(thiSinh.getThiSinhId());

                Toast.makeText(requireContext(), "Đã xóa " + thiSinh.getHoTen(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popupMenu.show();
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
    private void showDialogSuaThiSinh(ThiSinh thiSinhCanSua, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_them_thi_sinh);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Ánh xạ
        EditText edtThiSinhID = dialog.findViewById(R.id.edt_ThiSinh_ID);
        EditText edtHoTen = dialog.findViewById(R.id.edt_HoTen);
        Button btnHuy = dialog.findViewById(R.id.btn_Huy);
        Button btnLuu = dialog.findViewById(R.id.btn_Luu);

        // 1. ĐỔ DỮ LIỆU CŨ VÀO Ô NHẬP LÝ
        edtThiSinhID.setText(thiSinhCanSua.getThiSinhId());
        edtHoTen.setText(thiSinhCanSua.getHoTen());

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        // 2. XỬ LÝ KHI BẤM LƯU
        btnLuu.setOnClickListener(v -> {
            String maMoi = edtThiSinhID.getText().toString().trim();
            String tenMoi = edtHoTen.getText().toString().trim();

            if (maMoi.isEmpty() || tenMoi.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy ID cũ trước khi nó bị ghi đè
            String idCu = thiSinhCanSua.getThiSinhId();

            // Gọi hàm cập nhật DB
            boolean isUpdated = dbHelper.capNhatThiSinh(idCu, maMoi, tenMoi);

            if (isUpdated) {
                // Cập nhật lại Object Thí Sinh trong danh sách hiện tại
                thiSinhCanSua.setThiSinhId(maMoi);
                thiSinhCanSua.setHoTen(tenMoi);

                // Báo cho Adapter biết vị trí này đã thay đổi để nó vẽ lại giao diện
                thiSinhAdapter.notifyItemChanged(position);

                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Lỗi! Mã thí sinh mới có thể bị trùng với người khác.", Toast.LENGTH_SHORT).show();
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