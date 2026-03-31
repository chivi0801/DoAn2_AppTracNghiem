package com.example.android_python;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

public class AnswerKeyFragment extends Fragment {

    private RecyclerView rvChoices;
    private Button btnTabMaDe, btnTabDapAn;
    private TextView tvCurrentMode;
    private LinearLayout layoutHeaderLabels;

    private int questionCount = 30;
    private boolean isMaDeMode = true;

    // DỮ LIỆU GỐC - KHÔNG ĐƯỢC RESET LẠI Ở onCreateView
    private int[] maDeSelections = new int[3];
    private int[] dapAnSelections;
    private int editingPosition = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // 1. Lấy số câu hỏi
        if (getArguments() != null) {
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
        }

        // 2. KHỞI TẠO MẢNG TRỐNG (Chỉ chạy 1 lần duy nhất tại đây)
        dapAnSelections = new int[questionCount];
        Arrays.fill(maDeSelections, -1);
        Arrays.fill(dapAnSelections, -1);

        // 3. PHỤC HỒI DỮ LIỆU TỪ LỊCH SỬ
        if (getArguments() != null && getArguments().containsKey("EXISTING_KEY")) {
            SavedKey existing = (SavedKey) getArguments().getSerializable("EXISTING_KEY");
            editingPosition = getArguments().getInt("EDIT_POSITION", -1);

            if (existing != null) {
                // Phục hồi Mã đề (VD: "123" -> [1, 2, 3])
                String md = existing.getMaDe();
                for (int i = 0; i < 3 && i < md.length(); i++) {
                    char c = md.charAt(i);
                    maDeSelections[i] = (c == '?') ? -1 : Character.getNumericValue(c);
                }

                // Phục hồi Đáp án (VD: "ABCD" -> [0, 1, 2, 3])
                String da = existing.getDapAn();
                for (int i = 0; i < questionCount; i++) {
                    if (i < da.length()) {
                        char c = da.charAt(i);
                        // Nếu là '?' thì giữ -1, nếu là 'A' thì thành 0, 'B' thành 1...
                        dapAnSelections[i] = (c == '?') ? -1 : (c - 'A');
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_answer_key, container, false);

        rvChoices = v.findViewById(R.id.rvChoices);
        btnTabMaDe = v.findViewById(R.id.btnTabMaDe);
        btnTabDapAn = v.findViewById(R.id.btnTabDapAn);
        tvCurrentMode = v.findViewById(R.id.tvCurrentMode);
        layoutHeaderLabels = v.findViewById(R.id.layoutHeaderLabels);

        setupToolbar();
        rvChoices.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTabMaDe.setOnClickListener(view -> { if (!isMaDeMode) { isMaDeMode = true; updateUI(); } });
        btnTabDapAn.setOnClickListener(view -> { if (isMaDeMode) { isMaDeMode = false; updateUI(); } });

        updateUI();
        return v;
    }

    private void updateUI() {
        if (isMaDeMode) {
            btnTabMaDe.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00CCFF")));
            btnTabDapAn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            tvCurrentMode.setText("Mã Đề");
            if (layoutHeaderLabels != null) layoutHeaderLabels.setVisibility(View.GONE);

            // Dùng mảng maDeSelections đã phục hồi
            rvChoices.setAdapter(new ChoiceAdapter(10, 3, maDeSelections, true));
        } else {
            btnTabDapAn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00CCFF")));
            btnTabMaDe.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            tvCurrentMode.setText("Đáp Án");
            if (layoutHeaderLabels != null) {
                layoutHeaderLabels.setVisibility(View.VISIBLE);
                updateHeaderLabels();
            }

            // Dùng mảng dapAnSelections đã phục hồi (Lưu ý: Không được khởi tạo mới mảng ở đây!)
            rvChoices.setAdapter(new ChoiceAdapter(questionCount, 4, dapAnSelections, false));
        }
    }

    // Các hàm setupToolbar, updateHeaderLabels, onCreateOptionsMenu giữ nguyên như bạn đã làm...

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_answer_key, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_print) {
            Bundle result = new Bundle();
            result.putString("MA_DE", formatResult(maDeSelections, true));
            result.putString("DAP_AN", formatResult(dapAnSelections, false));
            result.putInt("EDIT_POSITION", editingPosition);

            getParentFragmentManager().setFragmentResult("requestKey", result);
            Toast.makeText(getContext(), "Đã lưu bộ đáp án!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatResult(int[] arr, boolean isMd) {
        StringBuilder sb = new StringBuilder();
        for (int s : arr) {
            if (s == -1) sb.append("?");
            else sb.append(isMd ? s : (char)('A' + s));
        }
        return sb.toString();
    }

    private void setupToolbar() {
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
    }

    private void updateHeaderLabels() {
        if (layoutHeaderLabels == null) return;
        layoutHeaderLabels.removeAllViews();
        String[] labels = {"A", "B", "C", "D"};
        for (String label : labels) {
            TextView tv = new TextView(getContext());
            tv.setText(label);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.parseColor("#003366"));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(16);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(130, -2);
            tv.setLayoutParams(p);
            layoutHeaderLabels.addView(tv);
        }
    }
}