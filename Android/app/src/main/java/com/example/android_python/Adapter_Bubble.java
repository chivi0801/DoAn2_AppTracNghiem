package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Adapter_Bubble extends RecyclerView.Adapter<Adapter_Bubble.ViewHolder> {

    // GÁN CỨNG: Luôn là 40 câu, không quan tâm biến bên ngoài
    private final int QUESTION_COUNT = 40;
    private List<String> listAnswers;

    // ĐÃ SỬA: Bỏ tham số questionCount ở constructor cho gọn
    public Adapter_Bubble(String existingData) {
        this.listAnswers = new ArrayList<>();

        // Vòng lặp luôn chạy 40 lần để tạo 40 hàng
        for (int i = 0; i < QUESTION_COUNT; i++) {
            if (existingData != null && i < existingData.length()) {
                listAnswers.add(String.valueOf(existingData.charAt(i)));
            } else {
                listAnswers.add(""); // Nếu là tạo mới hoặc dữ liệu cũ ngắn hơn 40, gán rỗng
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choice_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvRowNumber.setText(String.valueOf(position + 1));

        String currentAnswer = listAnswers.get(position);

        holder.rbA.setOnCheckedChangeListener(null);
        holder.rbB.setOnCheckedChangeListener(null);
        holder.rbC.setOnCheckedChangeListener(null);
        holder.rbD.setOnCheckedChangeListener(null);

        holder.rbA.setChecked("A".equals(currentAnswer));
        holder.rbB.setChecked("B".equals(currentAnswer));
        holder.rbC.setChecked("C".equals(currentAnswer));
        holder.rbD.setChecked("D".equals(currentAnswer));

        holder.rbD.setVisibility(View.VISIBLE);

        View.OnClickListener clickListener = v -> {
            String clickedAnswer = "";
            if (v == holder.rbA) clickedAnswer = "A";
            else if (v == holder.rbB) clickedAnswer = "B";
            else if (v == holder.rbC) clickedAnswer = "C";
            else if (v == holder.rbD) clickedAnswer = "D";

            if (!currentAnswer.equals(clickedAnswer)) {
                listAnswers.set(position, clickedAnswer);
                holder.itemView.post(() -> notifyItemChanged(position));
            }
        };

        holder.rbA.setOnClickListener(clickListener);
        holder.rbB.setOnClickListener(clickListener);
        holder.rbC.setOnClickListener(clickListener);
        holder.rbD.setOnClickListener(clickListener);
    }

    public String layChuoiDapAn() {
        StringBuilder builder = new StringBuilder();
        for (String ans : listAnswers) {
            if (ans.isEmpty()) {
                builder.append("X"); // Trả về X nếu giáo viên quên tô câu đó
            } else {
                builder.append(ans);
            }
        }
        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return QUESTION_COUNT; // Luôn trả về 40
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowNumber;
        RadioButton rbA, rbB, rbC, rbD;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRowNumber = itemView.findViewById(R.id.tvQuestionNumber);
            rbA = itemView.findViewById(R.id.rbA);
            rbB = itemView.findViewById(R.id.rbB);
            rbC = itemView.findViewById(R.id.rbC);
            rbD = itemView.findViewById(R.id.rbD);
        }
    }

    public List<String> getListAnswers() {
        return listAnswers;
    }
}