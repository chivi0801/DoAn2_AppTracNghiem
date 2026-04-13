package com.example.android_python;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_DsKyThi extends Fragment {
    private RecyclerView rvExams;
    private Adapter_KyThi adapter;
    private List<KyThi> examList = new ArrayList<>();
    private TaoCSDL dbHelper;
    private int currentGvId; // Biến lưu ID giảng viên hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_kythi, container, false);
        dbHelper = new TaoCSDL(getContext());

        // LẤY GV_ID TỪ SHAREDPREFERENCES
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentGvId = sharedPreferences.getInt("GV_ID", -1);

        setupToolbar();

        rvExams = view.findViewById(R.id.rvExams);
        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));

        // TẢI DỮ LIỆU THEO GV_ID
        // TẢI DỮ LIỆU THEO GV_ID
        loadExamsFromDatabase();

        // CẬP NHẬT LẠI ADAPTER VỚI LISTENER MỚI
        adapter = new Adapter_KyThi(examList, new Adapter_KyThi.OnExamActionListener() {
            @Override
            public void onItemClick(KyThi exam) {
                // Code mở chi tiết kỳ thi (giữ nguyên của ông)
                Fragment_ChiTietKyThi detailFragment = new Fragment_ChiTietKyThi();
                Bundle bundle = new Bundle();
                bundle.putString("EXAM_NAME", exam.getSubject());
                bundle.putInt("KYTHI_ID", exam.getExamId());
                detailFragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null).commit();
            }

            @Override
            public void onDeleteClick(KyThi exam, int position) {
                // GỌI HÀM XÁC NHẬN XÓA
                xacnhanXoa(exam, position);
            }
        });
        rvExams.setAdapter(adapter);

        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    private void loadExamsFromDatabase() {
        if (currentGvId != -1) {
            examList.clear();
            // Bạn cần đảm bảo hàm này trong TaoCSDL nhận tham số gvId
            examList.addAll(dbHelper.layDanhSachKyThiTheoGV(currentGvId));
        }
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) toolbarTitle.setText("Kiểm Tra");

            // HIỂN THỊ icon Menu và ẨN nút Quay về
            View iconMenu = activity.findViewById(R.id.icon_Menu);
            if (iconMenu != null) iconMenu.setVisibility(View.VISIBLE);

            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    // TẮT trượt để xóa
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                KyThi examToDelete = examList.get(position);

                // Xóa trong CSDL
                if (dbHelper.xoaKyThi(examToDelete.getExamId())) {
                    examList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Đã xóa kỳ thi", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyItemChanged(position); // Trả lại item nếu xóa lỗi
                    Toast.makeText(getContext(), "Lỗi khi xóa kỳ thi!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvExams);
    }

    private void showCreateExamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_tao_kythi);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edtSubject = dialog.findViewById(R.id.edtSubject);
        Spinner spnSheet = dialog.findViewById(R.id.spnSheet);

        String[] sheets = {"30", "40", "50"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sheets);
        spnSheet.setAdapter(spinnerAdapter);

        dialog.findViewById(R.id.btnConfirmCreate).setOnClickListener(v -> {
            String subject = edtSubject.getText().toString().trim();
            String loaiPhieu = spnSheet.getSelectedItem().toString();

            if (subject.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // GHI VÀO CSDL VỚI currentGvId THẬT (Đã bỏ qCount)
                boolean isInserted = dbHelper.ThemKyThi(currentGvId, subject, loaiPhieu);

                if (isInserted) {
                    loadExamsFromDatabase(); // Tải lại danh sách
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Tạo kỳ thi thành công!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    private void xacnhanXoa(KyThi item, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa kỳ thi")
                .setMessage("Bạn có chắc muốn xóa kỳ thi '" + item.getSubject() + "' không? Mọi dữ liệu đáp án bên trong sẽ mất hết!")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi hàm xóa trong CSDL
                    if (dbHelper.xoaKyThi(item.getExamId())) {
                        // Xóa thành công thì cập nhật giao diện
                        examList.remove(position);
                        adapter.notifyItemRemoved(position);
                        // Cập nhật lại index cho các item còn lại để tránh lỗi position
                        adapter.notifyItemRangeChanged(position, examList.size());

                        Toast.makeText(getContext(), "Đã xóa kỳ thi thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không thể xóa kỳ thi!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}