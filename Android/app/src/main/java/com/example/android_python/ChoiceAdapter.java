package com.example.android_python;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ViewHolder> {
    private int rowCount, colCount;
    private int[] data;
    private boolean isMaDe;

    public ChoiceAdapter(int row, int col, int[] data, boolean isMaDe) {
        this.rowCount = row;
        this.colCount = col;
        this.data = data;
        this.isMaDe = isMaDe;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choice_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvNumber.setText(String.valueOf(isMaDe ? position : position + 1));
        holder.container.removeAllViews();
        for (int i = 0; i < colCount; i++) {
            RadioButton rb = new RadioButton(holder.itemView.getContext());
            rb.setButtonDrawable(null);
            rb.setBackgroundResource(R.drawable.custom_radio_button);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(90, 90);
            p.setMargins(20, 10, 20, 10);
            rb.setLayoutParams(p);

            if (isMaDe) rb.setChecked(data[i] == position);
            else rb.setChecked(data[position] == i);

            final int col = i;
            final int row = position;
            rb.setOnClickListener(v -> {
                if (isMaDe) data[col] = row;
                else data[row] = col;
                notifyDataSetChanged();
            });
            holder.container.addView(rb);
        }
    }

    @Override
    public int getItemCount() { return rowCount; }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        LinearLayout container;
        ViewHolder(View v) {
            super(v);
            tvNumber = v.findViewById(R.id.tvRowNumber);
            container = v.findViewById(R.id.rgOptions);
        }
    }
}