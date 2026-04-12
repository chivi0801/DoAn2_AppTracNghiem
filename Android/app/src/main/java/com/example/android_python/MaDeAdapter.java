package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MaDeAdapter extends RecyclerView.Adapter<MaDeAdapter.ViewHolder> {

    // Mảng lưu 3 chữ số của mã đề (Hàng trăm, hàng chục, hàng đơn vị). Mặc định là "X"
    private String[] arrMaDe = {"X", "X", "X"};

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
        // position chạy từ 0 đến 9 tương ứng với các hàng số
        String number = String.valueOf(position);
        holder.tvRowNumber.setText(number);

        // 1. Gỡ listener cũ để tránh lỗi vòng lặp khi cuộn màn hình
        holder.rbA.setOnCheckedChangeListener(null);
        holder.rbB.setOnCheckedChangeListener(null);
        holder.rbC.setOnCheckedChangeListener(null);

        // 2. Set trạng thái UI dựa theo dữ liệu thực tế đang lưu trong mảng arrMaDe
        holder.rbA.setChecked(arrMaDe[0].equals(number));
        holder.rbB.setChecked(arrMaDe[1].equals(number));
        holder.rbC.setChecked(arrMaDe[2].equals(number));

        // Ẩn cột D vì mã đề chỉ có 3 chữ số
        holder.rbD.setVisibility(View.GONE);

        // 3. Xử lý sự kiện click (TƯƠNG TỰ NHƯ BUBBLE ADAPTER)
        View.OnClickListener clickListener = v -> {
            boolean isChanged = false;

            if (v == holder.rbA) {
                // Khóa cứng: Chỉ cho phép cập nhật nếu chọn số khác, không cho click lại để xóa
                if (!arrMaDe[0].equals(number)) {
                    arrMaDe[0] = number;
                    isChanged = true;
                }
            } else if (v == holder.rbB) {
                if (!arrMaDe[1].equals(number)) {
                    arrMaDe[1] = number;
                    isChanged = true;
                }
            } else if (v == holder.rbC) {
                if (!arrMaDe[2].equals(number)) {
                    arrMaDe[2] = number;
                    isChanged = true;
                }
            }

            // Nếu có sự thay đổi, bắt RecyclerView load lại toàn bộ danh sách.
            // Việc này sẽ tự động xóa dấu tick ở hàng cũ và tick vào hàng mới trên cùng 1 cột.
            if (isChanged) {
                holder.itemView.post(this::notifyDataSetChanged);
            }
        };

        // 4. Gắn sự kiện click vào các nút tròn
        holder.rbA.setOnClickListener(clickListener);
        holder.rbB.setOnClickListener(clickListener);
        holder.rbC.setOnClickListener(clickListener);
    }

    // Xuất chuỗi mã đề ra (VD: "105", hoặc "1X5" nếu giáo viên quên tô cột giữa)
    public String layChuoiMaDe() {
        return arrMaDe[0] + arrMaDe[1] + arrMaDe[2];
    }

    @Override
    public int getItemCount() {
        return 10; // Có 10 hàng từ số 0 đến số 9
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
}