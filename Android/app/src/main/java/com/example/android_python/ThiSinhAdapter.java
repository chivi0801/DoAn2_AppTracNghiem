package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ThiSinhAdapter extends RecyclerView.Adapter<ThiSinhAdapter.ThiSinhViewHolder> {

    private ArrayList<ThiSinh> listThiSinh;
    private OnItemClickListener listener;

    // Interface để bắt sự kiện
    public interface OnItemClickListener {
        void onItemClick(ThiSinh thiSinh); // Click vào cả dòng
        void onOptionsClick(ThiSinh thiSinh, View view, int position); // Click vào 3 chấm
    }

    // Hàm set listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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
            holder.tvMaThiSinh.setText("SBD: " + thiSinh.getThiSinhId());
            holder.tvHoTen.setText(thiSinh.getHoTen());

            // Bắt sự kiện click toàn bộ Item
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(thiSinh);
                }
            });

            // Bắt sự kiện click vào nút 3 chấm
            holder.ivMoreOptions.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOptionsClick(thiSinh, v, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listThiSinh != null ? listThiSinh.size() : 0;
    }

    public static class ThiSinhViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaThiSinh;
        TextView tvHoTen;
        ImageView ivMoreOptions;

        public ThiSinhViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaThiSinh = itemView.findViewById(R.id.tv_MaThiSinh);
            tvHoTen = itemView.findViewById(R.id.tv_HoTen);
            ivMoreOptions = itemView.findViewById(R.id.iv_MoreOptions);
        }
    }
}