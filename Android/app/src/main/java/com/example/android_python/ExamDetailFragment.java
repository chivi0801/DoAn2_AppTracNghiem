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

public class ExamDetailFragment extends Fragment {

    private String examName;
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;

    // Sử dụng static để danh sách mã đề không bị reset khi chuyển fragment qua lại
    private static List<SavedKey> savedKeyList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME");
        }

        // --- LOGIC NHẬN DỮ LIỆU TỪ AnswerKeyFragment ---
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String maDe = bundle.getString("MA_DE");
            String dapAn = bundle.getString("DAP_AN");
            int editPos = bundle.getInt("EDIT_POSITION", -1); // Nhận vị trí đang sửa

            if (maDe != null && dapAn != null) {
                SavedKey keyData = new SavedKey(maDe, dapAn);

                if (editPos != -1 && editPos < savedKeyList.size()) {
                    // TRƯỜNG HỢP SỬA: Ghi đè vào phần tử cũ
                    savedKeyList.set(editPos, keyData);
                    Toast.makeText(getContext(), "Đã cập nhật thay đổi!", Toast.LENGTH_SHORT).show();
                } else {
                    // TRƯỜNG HỢP THÊM MỚI: Add vào cuối list
                    savedKeyList.add(keyData);
                    Toast.makeText(getContext(), "Đã lưu bộ đáp án mới!", Toast.LENGTH_SHORT).show();
                }

                // Cập nhật giao diện list ngay lập tức
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exam_detail, container, false);

        // 1. Thiết lập RecyclerView
        rvSavedKeys = v.findViewById(R.id.layoutSavedKeys);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Thiết lập Adapter (Xử lý khi click vào item để XEM LẠI/SỬA)
        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            SavedKey item = savedKeyList.get(position);

            AnswerKeyFragment fragment = new AnswerKeyFragment();
            Bundle b = new Bundle();
            b.putInt("QUESTION_COUNT", 30);
            b.putSerializable("EXISTING_KEY", item); // Gửi dữ liệu cũ để hiện lại dấu tick
            b.putInt("EDIT_POSITION", position);    // Gửi vị trí để biết đường quay về ghi đè
            fragment.setArguments(b);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvSavedKeys.setAdapter(adapter);

        // 3. Thiết lập Vuốt sang trái để Xóa
        setupSwipeToDelete();

        // 4. Thiết lập Toolbar
        setupToolbar(v);

        // 5. Click "Đáp án" để THÊM MỚI
        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> {
            AnswerKeyFragment fragment = new AnswerKeyFragment();
            Bundle b = new Bundle();
            b.putInt("QUESTION_COUNT", 30);
            b.putInt("EDIT_POSITION", -1); // Đảm bảo là chế độ thêm mới
            fragment.setArguments(b);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 6. Nút chấm điểm
        v.findViewById(R.id.btnGrade).setOnClickListener(view ->
                Toast.makeText(getContext(), "Đang mở Camera chấm điểm: " + examName, Toast.LENGTH_SHORT).show());

        return v;
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                savedKeyList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa mã đề", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(rvSavedKeys);
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
        if (title != null) title.setText(examName);
    }
}