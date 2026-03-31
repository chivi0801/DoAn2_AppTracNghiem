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
import android.widget.TextView; // Thêm import này
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Thêm import này
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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

        // --- BỔ SUNG: RESET TOOLBAR KHI QUAY LẠI ---
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            // 1. Ẩn nút mũi tên quay lại
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            // 2. Đổi lại tiêu đề thành "Kiểm Tra"
            TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) {
                toolbarTitle.setText("Kiểm Tra");
            }
        }
        // ------------------------------------------

        rvExams = view.findViewById(R.id.rvExams);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        rvExams.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter và xử lý click chuyển trang
        adapter = new ExamAdapter(examList, exam -> {
            ExamDetailFragment detailFragment = new ExamDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("EXAM_NAME", exam.getSubject());
            detailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvExams.setAdapter(adapter);

        // Logic Vuốt để xóa
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                examList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa kỳ thi", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvExams);

        fabAdd.setOnClickListener(v -> showCreateExamDialog());

        return view;
    }

    private void showCreateExamDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_create_exam);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
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
            String subject = edtSubject.getText().toString().trim();
            String countStr = edtCount.getText().toString().trim();
            String sheet = spnSheet.getSelectedItem().toString();
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (!subject.isEmpty() && !countStr.isEmpty()) {
                examList.add(new Exam(subject, date, sheet, Integer.parseInt(countStr)));
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}