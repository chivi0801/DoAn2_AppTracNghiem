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
import java.util.ArrayList;

public class Adapter_Lop extends RecyclerView.Adapter<Adapter_Lop.ViewHolder> {

    private ArrayList<Lop> listData;
    private OnItemClickListener listener;

    // 1. Gộp cả 2 sự kiện: Click chuyển trang và Click xóa
    public interface OnItemClickListener {
        void onItemClick(Lop lop); // Thêm cái này
        void onDeleteClick(Lop lop, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public Adapter_Lop(ArrayList<Lop> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lop lop = listData.get(position);
        holder.tvItemSubject.setText(lop.getTenLop());
        holder.tvItemSheet.setText("Niên khóa: " + lop.getNienKhoa());

        // 2. BẮT SỰ KIỆN CLICK VÀO CARD LỚP ĐỂ CHUYỂN TRANG
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(lop);
            }
        });

        // 3. Xử lý sự kiện click vào nút 3 chấm để Xóa
        holder.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.ivMoreOptions);
            popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Xóa Lớp");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) {
                        listener.onDeleteClick(lop, position);
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        if (listData != null) return listData.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemSubject, tvItemSheet;
        ImageView ivMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemSubject = itemView.findViewById(R.id.tvItemSubject);
            tvItemSheet = itemView.findViewById(R.id.tvItemSheet);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
        }
    }
}