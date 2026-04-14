package com.example.android_python;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter_BaiThi extends RecyclerView.Adapter<Adapter_BaiThi.ViewHolder> {
    private Context context;
    private ArrayList<BaiThi> list;

    public Adapter_BaiThi(Context context, ArrayList<BaiThi> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bailamcuathisinh, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaiThi baiThi = list.get(position);
        holder.tvSBD.setText(baiThi.getThiSinhId());
        holder.tvMaDe.setText(baiThi.getMaDe());
        holder.tvDiem.setText(String.valueOf(baiThi.getTongDiem()));

        // Hiển thị ảnh tên và lớp từ đường dẫn
        if (baiThi.getAnhBaiLamTenThiSinh() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(baiThi.getAnhBaiLamTenThiSinh());
            holder.imgTen.setImageBitmap(bitmap);
        }
        if (baiThi.getAnhBaiLamLop() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(baiThi.getAnhBaiLamLop());
            holder.imgLop.setImageBitmap(bitmap);
        }

        holder.ivMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
            }
        });
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Xóa");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals("Xóa")) {
                    BaiThi baiThi = list.get(position);
                    TaoCSDL db = new TaoCSDL(context);
                    if (db.xoaBaiThi(baiThi.getBaiThiId())) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Đã xóa bài thi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi khi xóa dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTen, imgLop, ivMoreOptions;
        TextView tvSBD, tvMaDe, tvDiem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTen = itemView.findViewById(R.id.tenThiSinh_ROI);
            imgLop = itemView.findViewById(R.id.LopThiSinh_ROI);
            ivMoreOptions = itemView.findViewById(R.id.iv_MoreOptions);
            tvSBD = itemView.findViewById(R.id.tv_SBD);
            tvMaDe = itemView.findViewById(R.id.tv_MaDe);
            tvDiem = itemView.findViewById(R.id.tv_Diem);
        }
    }
}
