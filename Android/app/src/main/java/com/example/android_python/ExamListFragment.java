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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExamListFragment extends Fragment {
    private RecyclerView rvExams;
    private ExamAdapter adapter;
    private List<Exam> examList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_list, container, false);

        // 1. Cấu hình Toolbar cho trang chủ
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Không hiện nút quay lại ở trang chủ
            }
            TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) toolbarTitle.setText("Kiểm Tra");
        }

        // 2. Thiết lập RecyclerView
        rvExams = view.findViewById(R.id.rvExams);
        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ExamAdapter(examList, exam -> {
            // Chuyển sang trang chi tiết môn học
            ExamDetailFragment detailFragment = new ExamDetailFragment();
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

        // 4. Sự kiện bấm nút (+) để tạo kỳ thi mới
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    // --- HÀM XỬ LÝ VUỐT SANG TRÁI ĐỂ XÓA KỲ THI ---
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không dùng tính năng kéo thả đổi vị trí
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Lấy vị trí item bị vuốt
                int position = viewHolder.getAdapterPosition();

                // Xóa khỏi danh sách dữ liệu
                examList.remove(position);

                // Thông báo adapter để xóa kèm hiệu ứng trượt mượt mà
                adapter.notifyItemRemoved(position);

                Toast.makeText(getContext(), "Đã xóa kỳ thi thành công", Toast.LENGTH_SHORT).show();
            }
        };

        // Gắn bộ điều khiển vuốt vào RecyclerView
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvExams);
    }

    // --- HÀM HIỂN THỊ DIALOG TẠO KỲ THI ---
    private void showCreateExamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_exam);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edtSubject = dialog.findViewById(R.id.edtSubject);
        Spinner spnSheet = dialog.findViewById(R.id.spnSheet);

        // Danh sách số câu hỏi
        String[] sheets = {"30", "40", "50"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sheets);
        spnSheet.setAdapter(spinnerAdapter);

        dialog.findViewById(R.id.btnConfirmCreate).setOnClickListener(v -> {
            String subject = edtSubject.getText().toString().trim();
            String sheetValue = spnSheet.getSelectedItem().toString(); // Ví dụ: "40"
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (!subject.isEmpty()) {
                try {
                    // LỌC SỐ: Loại bỏ chữ "Phiếu" nếu có để tránh lỗi NumberFormatException
                    int qCount = Integer.parseInt(sheetValue.replaceAll("[^0-9]", ""));

                    // Thêm vào danh sách (Lưu sheetValue là "40" để hiển thị gọn gàng)
                    examList.add(new Exam(subject, date, sheetValue, qCount));

                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Đã tạo kỳ thi mới!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Lỗi định dạng số câu!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên môn học!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}