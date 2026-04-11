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
    private OnItemClickListener listener; // 1. THÊM BIẾN NÀY

    // 2. ĐỊNH NGHĨA INTERFACE (Nên để ở đây cho dễ gọi)
    public interface OnItemClickListener {
        void onItemClick(Exam exam);
    }

    // 3. CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN LISTENER
    public ExamAdapter(List<Exam> examList, OnItemClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);

        holder.tvSubject.setText(exam.getSubject());
        holder.tvDate.setText(exam.getDate());
        holder.tvSheet.setText("Phiếu: " + exam.getSheetType());
//

        // 4. GỌI HÀM BIND ĐÃ CÓ
        holder.bind(exam, listener);
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvSheet, tvCount;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvSheet = itemView.findViewById(R.id.tvItemSheet);
            tvCount = itemView.findViewById(R.id.tvItemCount);
        }

        // 5. GIỮ NGUYÊN HÀM BIND NÀY
        public void bind(final Exam exam, final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(exam);
                }
            });
        }
    }
}