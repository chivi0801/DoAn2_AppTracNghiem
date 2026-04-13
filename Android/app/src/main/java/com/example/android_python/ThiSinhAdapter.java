package com.example.android_python; // Thay bằng package của bạn

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ThiSinhAdapter extends RecyclerView.Adapter<ThiSinhAdapter.ThiSinhViewHolder> {

    private ArrayList<ThiSinh> listThiSinh;

    public ThiSinhAdapter(ArrayList<ThiSinh> listThiSinh) {
        this.listThiSinh = listThiSinh;
    }

    @NonNull
    @Override
    public ThiSinhViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thi_sinh, parent, false);
        return new ThiSinhViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThiSinhViewHolder holder, int position) {
        ThiSinh thiSinh = listThiSinh.get(position);

        if (thiSinh != null) {
            // Hiển thị dữ liệu lên TextView
            holder.tvMaThiSinh.setText("Mã TS: " + thiSinh.getThiSinhId());
            holder.tvHoTen.setText(thiSinh.getHoTen());
        }
    }

    @Override
    public int getItemCount() {
        if (listThiSinh != null) {
            return listThiSinh.size();
        }
        return 0;
    }

    // Lớp ViewHolder để ánh xạ các view trong item_thi_sinh.xml
    public static class ThiSinhViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaThiSinh;
        TextView tvHoTen;

        public ThiSinhViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaThiSinh = itemView.findViewById(R.id.tv_MaThiSinh);
            tvHoTen = itemView.findViewById(R.id.tv_HoTen);
        }
    }
}