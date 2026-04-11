package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavedKeyAdapter extends RecyclerView.Adapter<SavedKeyAdapter.ViewHolder> {
    private List<SavedKey> list;
    private OnItemClickListener listener;

    // 1. SỬA INTERFACE: Truyền vào "int position" thay vì "SavedKey item"
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SavedKeyAdapter(List<SavedKey> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_made, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedKey item = list.get(position);

        // Hiển thị mã đề vào TextView tvMaDe
        holder.tvMaDe.setText(item.getMaDe());

        // Bắt sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaDe;
        ViewHolder(View v) {
            super(v);
            tvMaDe = v.findViewById(R.id.tvMaDe);
        }
    }
}