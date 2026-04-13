package com.example.android_python;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Fragment_BaiThi extends Fragment {
    private RecyclerView rvBaiThi;
    private Adapter_BaiThi adapter;
    private ArrayList<BaiThi> listBaiThi;
    private TaoCSDL dbHelper;
    private int kyThiId = -1;
    private int lopId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baithi, container, false);

        if (getArguments() != null) {
            kyThiId = getArguments().getInt("KYTHI_ID", -1);
            lopId = getArguments().getInt("LOP_ID", -1);
        }

        rvBaiThi = view.findViewById(R.id.rvBaiThi);
        rvBaiThi.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new TaoCSDL(getContext());
        listBaiThi = dbHelper.layDanhSachBaiThi(kyThiId, lopId);

        adapter = new Adapter_BaiThi(getContext(), listBaiThi);
        rvBaiThi.setAdapter(adapter);

        return view;
    }
}