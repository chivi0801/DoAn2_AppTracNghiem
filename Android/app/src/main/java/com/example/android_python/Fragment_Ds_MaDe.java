package com.example.android_python;

import android.app.Dialog;
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

public class Fragment_Ds_DapAn extends Fragment {
    private RecyclerView rvExams;
    private ExamAdapter adapter;
    private List<Exam> examList = new ArrayList<>();
    private TaoCSDL dbHelper; // Khai báo CSDL

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_made, container, false);

        // Khởi tạo Database
        dbHelper = new TaoCSDL(getContext());

        // 1. Cấu hình Toolbar cho trang chủ
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) toolbarTitle.setText("Kiểm Tra");
        }

        // 2. Thiết lập RecyclerView & Lấy dữ liệu từ SQLite (Thay vì list trống)
        rvExams = view.findViewById(R.id.rvExams);
        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));

        // GỌI HÀM LẤY DỮ LIỆU TỪ SQLITE
        examList = dbHelper.layDanhSachKyThiDayDu();

        adapter = new ExamAdapter(examList, exam -> {
            Fragment_ChiTietKyThi detailFragment = new Fragment_ChiTietKyThi();
            Bundle bundle = new Bundle();
            bundle.putString("EXAM_NAME", exam.getSubject());
            bundle.putInt("QUESTION_COUNT", exam.getQuestionCount());

            detailFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null).commit();
        });
        rvExams.setAdapter(adapter);

        // 3. Kích hoạt tính năng vuốt để xóa
        setupSwipeToDelete();

        // 4. Sự kiện bấm nút (+) dưới cùng bên phải
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Ở đây lý tưởng nhất là bạn gọi thêm hàm dbHelper.xoaKyThi()
                // Nhưng tạm thời cứ xóa trên danh sách hiển thị trước
                examList.remove(position);
                adapter.notifyItemRemoved(position);

                Toast.makeText(getContext(), "Đã xóa kỳ thi", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvExams);
    }

    // --- HÀM HIỂN THỊ DIALOG TẠO KỲ THI KHI BẤM DẤU (+) ---
    private void showCreateExamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_tao_kythi);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Ánh xạ View theo đúng Hình 2 của bạn
        EditText edtSubject = dialog.findViewById(R.id.edtSubject); // Ô Tên môn
        EditText edtSoCau = dialog.findViewById(R.id.edtCount);     // Ô Nhập số lượng câu (Bổ sung thêm)
        Spinner spnSheet = dialog.findViewById(R.id.spnSheet);      // Spinner Loại phiếu

        String[] sheets = {"30", "40", "50"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sheets);
        spnSheet.setAdapter(spinnerAdapter);

        dialog.findViewById(R.id.btnConfirmCreate).setOnClickListener(v -> {
            String subject = edtSubject.getText().toString().trim();
            String soCauStr = edtSoCau.getText().toString().trim();
            String loaiPhieu = spnSheet.getSelectedItem().toString();

            // Kiểm tra nhập liệu
            if (subject.isEmpty() || soCauStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ Tên môn và Số câu!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int qCount = Integer.parseInt(soCauStr);

                // Giả sử GV_ID = 1 (Tạm thời fix cứng do chưa có đăng nhập)
                int gvId = 1;

                // 1. GHI DATA VÀO CSDL
                boolean isInserted = dbHelper.ThemKyThi(gvId, subject, loaiPhieu, qCount);

                if (isInserted) {
                    // 2. LÀM MỚI DANH SÁCH HIỂN THỊ (Load lại từ Database)
                    examList.clear();
                    examList.addAll(dbHelper.layDanhSachKyThiDayDu());
                    adapter.notifyDataSetChanged(); // Báo cho RecyclerView vẽ lại màn hình

                    dialog.dismiss(); // Tắt Dialog
                    Toast.makeText(getContext(), "Tạo kỳ thi thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lưu vào CSDL!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số lượng câu phải là số!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}