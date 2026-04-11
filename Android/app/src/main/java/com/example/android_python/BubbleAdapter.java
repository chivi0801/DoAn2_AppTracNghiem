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

public class BubbleAdapter extends RecyclerView.Adapter<BubbleAdapter.ViewHolder> {

    private int questionCount;
    // Bỏ mảng arrDapAn đi, CHỈ DÙNG 1 biến listAnswers cho đồng bộ
    private List<String> listAnswers;

    // CONSTRUCTOR ĐÃ ĐƯỢC SỬA LỖI
    public BubbleAdapter(int questionCount, String existingData) {
        this.questionCount = questionCount;

        // BẮT BUỘC PHẢI KHỞI TẠO LIST Ở ĐÂY ĐỂ TRÁNH LỖI VĂNG APP (NullPointerException)
        this.listAnswers = new ArrayList<>();

        for (int i = 0; i < questionCount; i++) {
            // Nếu có dữ liệu cũ thì load ra
            if (existingData != null && i < existingData.length()) {
                String ans = String.valueOf(existingData.charAt(i));
                // Nếu dữ liệu cũ là "X" (chưa chọn) thì đưa về chuỗi rỗng "" để UI xử lý
                if (ans.equals("X") || ans.equals("M")) {
                    listAnswers.add("");
                } else {
                    listAnswers.add(ans);
                }
            } else {
                // Mặc định ban đầu chưa chọn gì cả là chuỗi rỗng
                listAnswers.add("");
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

        // Lúc này listAnswers đã có dữ liệu nên không bị crash nữa
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

            if (currentAnswer.equals(clickedAnswer)) {
                listAnswers.set(position, ""); // Hủy chọn
            } else {
                listAnswers.set(position, clickedAnswer); // Chọn mới
            }
            holder.itemView.post(() -> notifyItemChanged(position));
        };

        holder.rbA.setOnClickListener(clickListener);
        holder.rbB.setOnClickListener(clickListener);
        holder.rbC.setOnClickListener(clickListener);
        holder.rbD.setOnClickListener(clickListener);
    }

    // Hàm xuất chuỗi đáp án ra để lưu vào CSDL (VD: "ABCDX...")
    public String layChuoiDapAn() {
        StringBuilder builder = new StringBuilder();
        for (String ans : listAnswers) {
            if (ans.isEmpty()) {
                builder.append("X"); // Nếu chuỗi rỗng (chưa chọn) thì lưu là X
            } else {
                builder.append(ans);
            }
        }
        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return questionCount;
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