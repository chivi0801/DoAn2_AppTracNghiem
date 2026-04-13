package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class Adapter_KyThi extends RecyclerView.Adapter<Adapter_KyThi.ExamViewHolder> {

    private List<KyThi> examList;
    private OnExamActionListener listener;

    // 1. CẬP NHẬT INTERFACE: Có cả click chọn và click xóa
    public interface OnExamActionListener {
        void onItemClick(KyThi exam);
        void onDeleteClick(KyThi exam, int position);
    }

    public Adapter_KyThi(List<KyThi> examList, OnExamActionListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kythi, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        KyThi exam = examList.get(position);

        holder.tvSubject.setText(exam.getSubject());
        holder.tvDate.setText(exam.getDate());
        holder.tvSheet.setText("Phiếu: " + exam.getSheetType());
//        holder.tvCount.setText(String.valueOf(exam.getQuestionCount())); // Nếu có TextView hiển thị số câu

        // 2. XỬ LÝ CLICK VÀO NÚT 3 CHẤM
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenu().add("Xóa");

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getTitle().equals("Xóa")) {
                    if (listener != null) {
                        listener.onDeleteClick(exam, position);
                    }
                }
                return true;
            });
            popupMenu.show();
        });

        // 3. XỬ LÝ CLICK VÀO NGUYÊN CÁI CARD (Mở chi tiết)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(exam);
            }
        });
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvSheet, tvCount;
        ImageButton btnMore; // THÊM NÚT 3 CHẤM VÀO ĐÂY

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvSheet = itemView.findViewById(R.id.tvItemSheet);
//            tvCount = itemView.findViewById(R.id.tvItemCount);
            btnMore = itemView.findViewById(R.id.btnItemMore); // ÁNH XẠ NÚT 3 CHẤM
        }
    }
}