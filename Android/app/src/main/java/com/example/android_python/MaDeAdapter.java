package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MaDeAdapter extends RecyclerView.Adapter<MaDeAdapter.ViewHolder> {

    // Mảng lưu 3 chữ số của mã đề (Hàng trăm, hàng chục, hàng đơn vị). Mặc định là "X" (chưa chọn)
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
        // Biến position chạy từ 0 đến 9 tương ứng với các số cần bôi đen
        String number = String.valueOf(position);
        holder.tvRowNumber.setText(number);

        // 1. Gỡ bỏ sự kiện lắng nghe cũ để tránh lỗi tự động kích hoạt sai khi cuộn màn hình
        holder.rbA.setOnCheckedChangeListener(null);
        holder.rbB.setOnCheckedChangeListener(null);
        holder.rbC.setOnCheckedChangeListener(null);

        // 2. So sánh mảng dữ liệu với số của hàng hiện tại để quyết định xem ô nào được tick
        holder.rbA.setChecked(arrMaDe[0].equals(number));
        holder.rbB.setChecked(arrMaDe[1].equals(number));
        holder.rbC.setChecked(arrMaDe[2].equals(number));

        // Ẩn cột D vì mã đề thường chỉ có 3 chữ số (3 cột)
        holder.rbD.setVisibility(View.GONE);

        // 3. Xử lý sự kiện khi người dùng bấm chạm vào một ô tròn
        android.widget.CompoundButton.OnCheckedChangeListener listener = (btn, isChecked) -> {
            if (isChecked) {
                // Nếu người dùng tick chọn, lưu số của hàng này vào đúng cột (A=0, B=1, C=2)
                if (btn == holder.rbA) arrMaDe[0] = number;
                if (btn == holder.rbB) arrMaDe[1] = number;
                if (btn == holder.rbC) arrMaDe[2] = number;
            } else {
                // Nếu người dùng bỏ tick chính ô đang chọn, gán lại thành "X"
                if (btn == holder.rbA && arrMaDe[0].equals(number)) arrMaDe[0] = "X";
                if (btn == holder.rbB && arrMaDe[1].equals(number)) arrMaDe[1] = "X";
                if (btn == holder.rbC && arrMaDe[2].equals(number)) arrMaDe[2] = "X";
            }

            // QUAN TRỌNG NHẤT: Báo cho Adapter biết dữ liệu đã thay đổi để nó load lại danh sách.
            // Việc này giúp xóa dấu tick ở các hàng khác nằm trên CÙNG 1 CỘT (tạo hiệu ứng RadioButton dọc)
            notifyDataSetChanged();
        };

        // 4. Gắn sự kiện lắng nghe vào các nút
        holder.rbA.setOnCheckedChangeListener(listener);
        holder.rbB.setOnCheckedChangeListener(listener);
        holder.rbC.setOnCheckedChangeListener(listener);
    }

    // Hàm này sẽ được gọi ở Activity/Fragment để lấy kết quả mã đề (VD: "101")
    public String layChuoiMaDe() {
        return arrMaDe[0] + arrMaDe[1] + arrMaDe[2];
    }

    @Override
    public int getItemCount() {
        return 10; // Cột dọc luôn có 10 hàng (từ số 0 đến số 9)
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowNumber;
        RadioButton rbA, rbB, rbC, rbD; // Khai báo đúng kiểu RadioButton

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng các ID trong file item_choice_row.xml
            tvRowNumber = itemView.findViewById(R.id.tvQuestionNumber);
            rbA = itemView.findViewById(R.id.rbA);
            rbB = itemView.findViewById(R.id.rbB);
            rbC = itemView.findViewById(R.id.rbC);
            rbD = itemView.findViewById(R.id.rbD);
        }
    }
}