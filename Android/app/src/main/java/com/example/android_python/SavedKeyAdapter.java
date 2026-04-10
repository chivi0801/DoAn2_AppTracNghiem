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
        // Sử dụng layout mặc định của Android cho nhanh và sạch
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedKey item = list.get(position);

        // Hiển thị mã đề
        holder.textView.setText("Mã đề: " + item.getMaDe());

        // Trang trí một chút cho giống bản cũ của bạn
        holder.textView.setPadding(45, 45, 45, 45);
        holder.textView.setTextSize(18);
        holder.textView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // 2. SỬA SỰ KIỆN CLICK: Truyền cái "position" này vào listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View v) {
            super(v);
            // simple_list_item_1 của Android có ID mặc định là text1
            textView = v.findViewById(android.R.id.text1);
        }
    }
}