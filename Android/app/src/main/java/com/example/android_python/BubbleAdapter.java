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
    private List<String> listAnswers;

    public BubbleAdapter(int questionCount, String existingData) {
        this.questionCount = questionCount;
        this.listAnswers = new ArrayList<>();

        // Code đã được làm gọn: Bỏ lọc X, M. Trực tiếp nạp dữ liệu chuẩn.
        for (int i = 0; i < questionCount; i++) {
            if (existingData != null && i < existingData.length()) {
                listAnswers.add(String.valueOf(existingData.charAt(i)));
            } else {
                listAnswers.add(""); // Tạo mới thì mặc định là chưa chọn (rỗng)
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

        // Xóa listener cũ trước khi set trạng thái để tránh lỗi loop
        holder.rbA.setOnCheckedChangeListener(null);
        holder.rbB.setOnCheckedChangeListener(null);
        holder.rbC.setOnCheckedChangeListener(null);
        holder.rbD.setOnCheckedChangeListener(null);

        // Hiển thị đáp án đã chọn
        holder.rbA.setChecked("A".equals(currentAnswer));
        holder.rbB.setChecked("B".equals(currentAnswer));
        holder.rbC.setChecked("C".equals(currentAnswer));
        holder.rbD.setChecked("D".equals(currentAnswer));

        holder.rbD.setVisibility(View.VISIBLE);

        // Bắt sự kiện khi click
        View.OnClickListener clickListener = v -> {
            String clickedAnswer = "";
            if (v == holder.rbA) clickedAnswer = "A";
            else if (v == holder.rbB) clickedAnswer = "B";
            else if (v == holder.rbC) clickedAnswer = "C";
            else if (v == holder.rbD) clickedAnswer = "D";

            // Bắt buộc 1 câu 1 đáp án: Chỉ cho phép đổi sang đáp án khác, không cho click lại để hủy
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

    // Xuất chuỗi đáp án (VD: AABCD...). Nếu giáo viên bỏ sót câu nào thì nó ra chữ "X" để Activity bắt lỗi.
    public String layChuoiDapAn() {
        StringBuilder builder = new StringBuilder();
        for (String ans : listAnswers) {
            if (ans.isEmpty()) {
                builder.append("X");
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