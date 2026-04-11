package com.example.android_python;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.List;

public class SavedKeyAdapter extends RecyclerView.Adapter<SavedKeyAdapter.ViewHolder> {

    private List<SavedKey> list;
    private OnMaDeActionListener listener;

    // Interface để gửi lệnh Xóa và Sửa về Fragment
    public interface OnMaDeActionListener {
        void onItemClick(SavedKey item, int position); // Bấm vào để Sửa
        void onDeleteClick(SavedKey item, int position); // Bấm vào Xóa
    }

    public SavedKeyAdapter(List<SavedKey> list, OnMaDeActionListener listener) {
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

        // Gán mã đề vào TextView có ID là tvMaDe
        holder.tvMaDe.setText(item.getMaDe()); // Vì trong XML bạn đã có chữ "Mã đề" riêng rồi nên ở đây chỉ cần in con số ra thôi

        // Bấm vào cả thẻ để chỉnh sửa
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item, position));

        // Xử lý nút 3 chấm với ID là ivMoreOptions
        holder.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.ivMoreOptions);
            popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Xóa");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    listener.onDeleteClick(item, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo biến
        TextView tvMaDe;
        ImageView ivMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ĐÚNG tên ID trong file XML bạn vừa gửi
            tvMaDe = itemView.findViewById(R.id.tvMaDe);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
        }
    }
}