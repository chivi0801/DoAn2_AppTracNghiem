package com.example.android_python;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast; // Nhớ thêm import này

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper; // THÊM IMPORT NÀY
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        rvExams = view.findViewById(R.id.rvExams);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        // 1. Cài đặt RecyclerView (Giữ nguyên)
        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExamAdapter(examList);
        rvExams.setAdapter(adapter);

        // --- 2. THÊM LOGIC VUỐT XÓA VÀO ĐÂY ---
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không dùng tính năng sắp xếp lại (kéo thả)
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Lấy vị trí item bị vuốt
                int position = viewHolder.getAdapterPosition();

                // Xóa khỏi danh sách dữ liệu
                examList.remove(position);

                // Thông báo cho adapter để xóa dòng đó kèm hiệu ứng
                adapter.notifyItemRemoved(position);

                Toast.makeText(getContext(), "Đã xóa kỳ thi", Toast.LENGTH_SHORT).show();
            }
        };

        // Gắn bộ xử lý vuốt vào RecyclerView
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvExams);
        // --------------------------------------

        fabAdd.setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    // Hàm showCreateExamDialog giữ nguyên như cũ...
    private void showCreateExamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_exam);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        EditText edtSubject = dialog.findViewById(R.id.edtSubject);
        EditText edtCount = dialog.findViewById(R.id.edtCount);
        Spinner spnSheet = dialog.findViewById(R.id.spnSheet);
        Button btnCreate = dialog.findViewById(R.id.btnConfirmCreate);

        String[] sheets = {"Phiếu 30", "Phiếu 40", "Phiếu 50"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sheets);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSheet.setAdapter(spinnerAdapter);

        btnCreate.setOnClickListener(v -> {
            String subject = edtSubject.getText().toString();
            String count = edtCount.getText().toString();
            String sheet = spnSheet.getSelectedItem().toString();
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (!subject.isEmpty() && !count.isEmpty()) {
                examList.add(new Exam(subject, date, sheet, Integer.parseInt(count)));
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}