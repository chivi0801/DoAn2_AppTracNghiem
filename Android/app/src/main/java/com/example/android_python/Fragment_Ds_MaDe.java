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

import java.util.List;

public class Fragment_Ds_MaDe extends Fragment {
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;
    private List<SavedKey> savedKeyList;
    private int questionCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_made, container, false);

        if (getArguments() != null) {
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
        }

        // Lấy danh sách từ Fragment_ChiTietKyThi (để tạm thời dùng chung dữ liệu)
        savedKeyList = Fragment_ChiTietKyThi.savedKeyList;

        setupToolbar(view);

        rvSavedKeys = view.findViewById(R.id.rvExams); // rvExams là ID RecyclerView trong fragment_ds_made.xml
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            openEditAnswerKey(savedKeyList.get(position), position);
        });
        rvSavedKeys.setAdapter(adapter);

        // Nút thêm (+) giống Activity_Main
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> openEditAnswerKey(null, -1));

        setupSwipeToDelete();

        // Nhận kết quả trả về khi Lưu đáp án
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String maDe = bundle.getString("MA_DE");
            String dapAn = bundle.getString("DAP_AN");
            int editPos = bundle.getInt("EDIT_POSITION", -1);
            if (maDe != null && dapAn != null) {
                if (editPos != -1 && editPos < savedKeyList.size()) {
                    savedKeyList.set(editPos, new SavedKey(maDe, dapAn));
                } else {
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
                savedKeyList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa mã đề", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSavedKeys);
    }
}