package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MaDeAdapter extends RecyclerView.Adapter<MaDeAdapter.ViewHolder> {

    private String[] arrMaDe = {"X", "X", "X"};

    // HÀM NÀY GIẢI QUYẾT LỖI ĐỎ CỦA BẠN ĐÂY!
    public MaDeAdapter(String existingData) {
        if (existingData != null && existingData.length() >= 3) {
            arrMaDe[0] = String.valueOf(existingData.charAt(0));
            arrMaDe[1] = String.valueOf(existingData.charAt(1));
            arrMaDe[2] = String.valueOf(existingData.charAt(2));
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
        String number = String.valueOf(position);
        holder.tvRowNumber.setText(number);

        holder.cbA.setText("");
        holder.cbB.setText("");
        holder.cbC.setText("");
        holder.cbD.setVisibility(View.GONE); // Ẩn ô D đi

        holder.cbA.setOnCheckedChangeListener(null);
        holder.cbB.setOnCheckedChangeListener(null);
        holder.cbC.setOnCheckedChangeListener(null);

        holder.cbA.setChecked(arrMaDe[0].equals(number));
        holder.cbB.setChecked(arrMaDe[1].equals(number));
        holder.cbC.setChecked(arrMaDe[2].equals(number));

        android.widget.CompoundButton.OnCheckedChangeListener listener = (btn, isChecked) -> {
            if (isChecked) {
                if (btn == holder.cbA) arrMaDe[0] = number;
                if (btn == holder.cbB) arrMaDe[1] = number;
                if (btn == holder.cbC) arrMaDe[2] = number;
            } else {
                if (btn == holder.cbA && arrMaDe[0].equals(number)) arrMaDe[0] = "X";
                if (btn == holder.cbB && arrMaDe[1].equals(number)) arrMaDe[1] = "X";
                if (btn == holder.cbC && arrMaDe[2].equals(number)) arrMaDe[2] = "X";
            }
            // Chỉ notify những cột thay đổi để UI mượt hơn
            notifyDataSetChanged();
        };

        holder.cbA.setOnCheckedChangeListener(listener);
        holder.cbB.setOnCheckedChangeListener(listener);
        holder.cbC.setOnCheckedChangeListener(listener);
    }

    public String layChuoiMaDe() {
        return arrMaDe[0] + arrMaDe[1] + arrMaDe[2];
    }

    @Override
    public int getItemCount() {
        return 10;
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