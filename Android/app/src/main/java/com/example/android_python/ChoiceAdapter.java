package com.example.android_python;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ViewHolder> {

    private int rowCount;
    private int columnCount;
    private int[] selections; // Mảng dữ liệu dùng chung với Fragment
    private boolean isMaDeMode;

    public ChoiceAdapter(int rowCount, int columnCount, int[] selections, boolean isMaDeMode) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.selections = selections;
        this.isMaDeMode = isMaDeMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choice_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 1. Hiển thị số thứ tự (Mã đề: 0-9 | Đáp án: 1-N)
        if (isMaDeMode) {
            holder.tvNumber.setText(String.valueOf(position));
        } else {
            holder.tvNumber.setText(String.valueOf(position + 1));
        }

        // 2. Làm sạch hàng trước khi vẽ lại để tránh lỗi chồng View
        holder.containerOptions.removeAllViews();

        // 3. Kiểm tra an toàn: Nếu mảng bị null thì không vẽ ô tròn để tránh Crash
        if (selections == null) return;

        // 4. Vẽ các ô tròn (RadioButton)
        for (int i = 0; i < columnCount; i++) {
            RadioButton rb = new RadioButton(holder.itemView.getContext());

            // Cấu hình giao diện ô tròn
            rb.setButtonDrawable(null);
            rb.setBackgroundResource(R.drawable.custom_radio_button);

            // Đặt kích thước cố định (90x90)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(90, 90);
            params.setMargins(20, 10, 20, 10);
            rb.setLayoutParams(params);

            // 5. LOGIC HIỂN THỊ TICK (Đây là phần quan trọng nhất)
            if (isMaDeMode) {
                // CHẾ ĐỘ MÃ ĐỀ: i là cột (0,1,2), position là hàng (0-9)
                // Phải check độ dài mảng để tránh IndexOutOfBounds
                if (i < selections.length) {
                    rb.setChecked(selections[i] == position);
                }
            } else {
                // CHẾ ĐỘ ĐÁP ÁN: position là hàng (câu hỏi), i là cột (A,B,C,D)
                if (position < selections.length) {
                    rb.setChecked(selections[position] == i);
                }
            }

            final int columnIndex = i;
            final int rowPosition = position;

            // 6. XỬ LÝ SỰ KIỆN CLICK
            rb.setOnClickListener(v -> {
                if (isMaDeMode) {
                    // Nếu bấm lại ô cũ thì bỏ chọn (-1), nếu không thì chọn hàng đó
                    if (selections[columnIndex] == rowPosition) selections[columnIndex] = -1;
                    else selections[columnIndex] = rowPosition;
                } else {
                    // Tương tự cho Đáp án
                    if (selections[rowPosition] == columnIndex) selections[rowPosition] = -1;
                    else selections[rowPosition] = columnIndex;
                }
                // Vẽ lại giao diện để cập nhật dấu tick mới
                notifyDataSetChanged();
            });

            holder.containerOptions.addView(rb);
        }
    }

    @Override
    public int getItemCount() {
        return rowCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        LinearLayout containerOptions;

        public ViewHolder(View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tvRowNumber);
            containerOptions = v.findViewById(R.id.rgOptions);
        }
    }
}