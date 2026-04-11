package com.example.android_python;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Ds_MaDe extends Fragment {
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;
    private List<SavedKey> savedKeyList = new ArrayList<>();

    private int questionCount;
    private int kyThiId = -1; // Thêm biến hứng ID kỳ thi
    private TaoCSDL dbHelper; // Thêm biến gọi CSDL

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new TaoCSDL(getContext());

        if (getArguments() != null) {
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
            kyThiId = getArguments().getInt("KYTHI_ID", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_made, container, false);

        setupToolbar(view);

        // Lấy dữ liệu trực tiếp từ CSDL theo KyThi_ID thay vì lấy tĩnh từ Fragment khác
        savedKeyList.clear();
        if (kyThiId != -1) {
            savedKeyList.addAll(dbHelper.layDanhSachMaDe(kyThiId));
        }

        rvSavedKeys = view.findViewById(R.id.rvExams);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            openEditAnswerKey(savedKeyList.get(position), position);
        });
        rvSavedKeys.setAdapter(adapter);

        // Nút thêm (+)
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> openEditAnswerKey(null, -1));

        setupSwipeToDelete();

        // Xử lý sự kiện khi màn hình Nhập Đáp Án bấm Lưu gửi về
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String maDe = bundle.getString("MA_DE");
            String dapAn = bundle.getString("DAP_AN");
            int editPos = bundle.getInt("EDIT_POSITION", -1);

            if (maDe != null && dapAn != null && kyThiId != -1) {
                if (editPos != -1 && editPos < savedKeyList.size()) {
                    // Cập nhật (Sửa) trong CSDL
                    SavedKey oldKey = savedKeyList.get(editPos);
                    dbHelper.suaMaDe(kyThiId, oldKey.getMaDe(), maDe, dapAn);

                    savedKeyList.set(editPos, new SavedKey(maDe, dapAn));
                } else {
                    // Thêm mới vào CSDL
                    dbHelper.themMaDe(kyThiId, maDe, dapAn);

                    savedKeyList.add(new SavedKey(maDe, dapAn));
                }
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private void setupToolbar(View v) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            TextView toolbarTitle = activity.findViewById(R.id.toolbar_title);
            if (toolbarTitle != null) toolbarTitle.setText("Danh Sách Mã Đề");

            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
            }
        }
    }

    private void openEditAnswerKey(SavedKey item, int pos) {
        Fragment_ChiTiet_DapAn fragment = new Fragment_ChiTiet_DapAn();
        Bundle b = new Bundle();
        b.putInt("QUESTION_COUNT", questionCount);
        if (item != null) {
            b.putSerializable("EXISTING_KEY", item);
            b.putInt("EDIT_POSITION", pos);
        }
        fragment.setArguments(b);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // 1. Xóa trong CSDL trước
                SavedKey deletedKey = savedKeyList.get(position);
                if (kyThiId != -1) {
                    dbHelper.xoaMaDe(kyThiId, deletedKey.getMaDe());
                }

                // 2. Xóa trên giao diện
                savedKeyList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa mã đề", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSavedKeys);
    }
}