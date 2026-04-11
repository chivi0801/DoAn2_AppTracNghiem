package com.example.android_python;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private String examName;
    private RecyclerView rvStatistics;
    private StatisticsAdapter adapter;
    private List<StatisticsResult> dataList; // Khai báo list dữ liệu

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_thong_ke, container, false);

        setupToolbar(v);

        rvStatistics = v.findViewById(R.id.rvStatistics);
        rvStatistics.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- KHỞI TẠO LIST TRỐNG ---
        dataList = new ArrayList<>();

        // Gán adapter với list trống, màn hình sẽ chỉ hiện Header
        adapter = new StatisticsAdapter(dataList);
        rvStatistics.setAdapter(adapter);

        return v;
    }

    private void setupToolbar(View v) {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        }
        TextView title = getActivity().findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Thống kê: " + examName);
    }
}