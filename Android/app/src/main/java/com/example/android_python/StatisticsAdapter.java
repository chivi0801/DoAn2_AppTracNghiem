package com.example.android_python;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.ViewHolder> {
    private List<StatisticsResult> list;

    public StatisticsAdapter(List<StatisticsResult> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statistics, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatisticsResult item = list.get(position);
        holder.tvMaTS.setText(item.getMaTS());
        holder.tvMaDe.setText(item.getMaDe());
        holder.tvCauDung.setText(item.getCauDung());
        holder.tvDiem.setText(item.getDiem());
    }

    @Override
    public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaTS, tvMaDe, tvCauDung, tvDiem;
        ViewHolder(View v) {
            super(v);
            tvMaTS = v.findViewById(R.id.tvMaTS);
            tvMaDe = v.findViewById(R.id.tvMaDe);
            tvCauDung = v.findViewById(R.id.tvCauDung);
            tvDiem = v.findViewById(R.id.tvDiem);
        }
    }
}