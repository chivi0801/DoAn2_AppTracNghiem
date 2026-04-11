package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BubbleAdapter extends RecyclerView.Adapter<BubbleAdapter.ViewHolder> {

    private int questionCount;
    private String[] arrDapAn;

    // CHÍNH LÀ HÀM NÀY ĐÂY: Hàm tạo nhận 2 tham số để hết báo đỏ!
    public BubbleAdapter(int questionCount, String existingData) {
        this.questionCount = questionCount;
        this.arrDapAn = new String[questionCount];

        for (int i = 0; i < questionCount; i++) {
            // Nếu có dữ liệu cũ thì load ra, không có thì mặc định là X
            if (existingData != null && i < existingData.length()) {
                arrDapAn[i] = String.valueOf(existingData.charAt(i));
            } else {
                arrDapAn[i] = "X";
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

        holder.cbA.setOnCheckedChangeListener(null);
        holder.cbB.setOnCheckedChangeListener(null);
        holder.cbC.setOnCheckedChangeListener(null);
        holder.cbD.setOnCheckedChangeListener(null);

        // Hiển thị trạng thái
        holder.cbA.setChecked(arrDapAn[position].equals("A") || arrDapAn[position].equals("M"));
        holder.cbB.setChecked(arrDapAn[position].equals("B") || arrDapAn[position].equals("M"));
        holder.cbC.setChecked(arrDapAn[position].equals("C") || arrDapAn[position].equals("M"));
        holder.cbD.setChecked(arrDapAn[position].equals("D") || arrDapAn[position].equals("M"));

        // Lắng nghe sự kiện
        android.widget.CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            arrDapAn[position] = kiemTraDapAn(holder);
        };

        holder.cbA.setOnCheckedChangeListener(listener);
        holder.cbB.setOnCheckedChangeListener(listener);
        holder.cbC.setOnCheckedChangeListener(listener);
        holder.cbD.setOnCheckedChangeListener(listener);
    }

    private String kiemTraDapAn(ViewHolder holder) {
        int count = 0;
        String answer = "";

        if (holder.cbA.isChecked()) { count++; answer = "A"; }
        if (holder.cbB.isChecked()) { count++; answer = "B"; }
        if (holder.cbC.isChecked()) { count++; answer = "C"; }
        if (holder.cbD.isChecked()) { count++; answer = "D"; }

        if (count == 0) return "X";
        if (count > 1) return "M";
        return answer;
    }

    public String layChuoiDapAn() {
        StringBuilder builder = new StringBuilder();
        for (String ans : arrDapAn) {
            builder.append(ans);
        }
        return builder.toString();
    }

    @Override
    public int getItemCount() {
        return questionCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowNumber;
        CheckBox cbA, cbB, cbC, cbD;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRowNumber = itemView.findViewById(R.id.tvRowNumber);
            cbA = itemView.findViewById(R.id.cbA);
            cbB = itemView.findViewById(R.id.cbB);
            cbC = itemView.findViewById(R.id.cbC);
            cbD = itemView.findViewById(R.id.cbD);
        }
    }
}