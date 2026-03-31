package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList;

    // Constructor để nhận danh sách kỳ thi
    public ExamAdapter(List<Exam> examList) {
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp giao diện item_exam.xml vào từng dòng của RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        // Lấy dữ liệu kỳ thi tại vị trí hiện tại
        Exam exam = examList.get(position);

        // Đổ dữ liệu vào các TextView
        holder.tvSubject.setText(exam.getSubject());
        holder.tvDate.setText(exam.getDate());
        holder.tvSheet.setText("Phiếu: " + exam.getSheetType());
        holder.tvCount.setText(String.valueOf(exam.getQuestionCount()));
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    // Lớp giữ các thành phần giao diện của mỗi dòng
    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvSheet, tvCount;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvSheet = itemView.findViewById(R.id.tvItemSheet);
            tvCount = itemView.findViewById(R.id.tvItemCount);
        }
    }
}