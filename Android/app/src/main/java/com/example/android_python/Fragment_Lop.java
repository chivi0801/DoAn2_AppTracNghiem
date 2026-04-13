package com.example.android_python;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Fragment_Lop extends Fragment {
    private RecyclerView rvLop;
    private LopAdapter lopAdapter;
    private ArrayList<Lop> listLop;
    private TaoCSDL dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_kythi, container, false);

        dbHelper = new TaoCSDL(getContext());
        rvLop = view.findViewById(R.id.rvExams);
        rvLop.setLayoutManager(new LinearLayoutManager(getContext()));

        if (view.findViewById(R.id.fabAdd) != null) {
            view.findViewById(R.id.fabAdd).setVisibility(View.GONE);
        }

        setupToolbar();
        loadData();

        return view;
    }

    private void loadData() {
        int gvId = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("GV_ID", -1);
        listLop = dbHelper.layDanhSachLopDuyNhat(gvId);

        lopAdapter = new LopAdapter(listLop);


        rvLop.setAdapter(lopAdapter);
    }

    private void confirmDelete(Lop lop, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn muốn ẩn lớp " + lop.getTenLop() + " khỏi danh sách này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    listLop.remove(position);
                    lopAdapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Đã xóa khỏi danh sách", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            TextView title = activity.findViewById(R.id.toolbar_title);
            if (title != null) title.setText("Danh Sách Lớp");
        }
    }
}