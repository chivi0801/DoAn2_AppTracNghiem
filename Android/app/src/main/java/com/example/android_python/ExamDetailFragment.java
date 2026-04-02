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
    private int questionCount;
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;
    private static List<SavedKey> savedKeyList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME");
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
        }

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
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exam_detail, container, false);
        rvSavedKeys = v.findViewById(R.id.layoutSavedKeys);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            openAnswerKey(savedKeyList.get(position), position);
        });
        rvSavedKeys.setAdapter(adapter);

        // --- GỌI LẠI HÀM VUỐT ĐỂ XÓA TẠI ĐÂY ---
        setupSwipeToDelete();

        setupToolbar(v);

        // GIỮ NGUYÊN CODE CŨ
        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> openAnswerKey(null, -1));

        // --- THÊM TÁC VỤ NÚT CHẤM ĐIỂM TẠI ĐÂY ---
        v.findViewById(R.id.btnGrade).setOnClickListener(view -> {
            if (savedKeyList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập Đáp Án trước khi chấm điểm!", Toast.LENGTH_SHORT).show();
            } else {
                openGradeFragment();
            }
        });
        v.findViewById(R.id.cardStats).setOnClickListener(view -> {
            StatisticsFragment fragment = new StatisticsFragment();
            Bundle b = new Bundle();
            b.putString("EXAM_NAME", examName);
            fragment.setArguments(b);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit();
        });

        return v;
    }

    // --- THÊM HÀM MỞ TRANG CHẤM ĐIỂM (KHÔNG XÓA CODE CŨ) ---
    private void openGradeFragment() {
        GradeFragment fragment = new GradeFragment();
        Bundle b = new Bundle();
        b.putString("EXAM_NAME", examName);
        fragment.setArguments(b);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // --- HÀM XỬ LÝ VUỐT SANG TRÁI ĐỂ XÓA (GIỮ NGUYÊN) ---
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // Không dùng kéo thả đổi vị trí
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Lấy vị trí phần tử bị vuốt
                int position = viewHolder.getAdapterPosition();

                // Xóa khỏi danh sách dữ liệu
                savedKeyList.remove(position);

                // Thông báo adapter xóa item kèm hiệu ứng
                adapter.notifyItemRemoved(position);

                Toast.makeText(getContext(), "Đã xóa bộ mã đề", Toast.LENGTH_SHORT).show();
            }
        };

        // Gắn vào RecyclerView
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSavedKeys);
    }

    // GIỮ NGUYÊN HÀM openAnswerKey
    private void openAnswerKey(SavedKey item, int pos) {
        AnswerKeyFragment fragment = new AnswerKeyFragment();
        Bundle b = new Bundle();
        b.putInt("QUESTION_COUNT", questionCount);
        if (item != null) {
            b.putSerializable("EXISTING_KEY", item);
            b.putInt("EDIT_POSITION", pos);
        }
        fragment.setArguments(b);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // GIỮ NGUYÊN HÀM setupToolbar
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