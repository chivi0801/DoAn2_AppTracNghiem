package com.example.android_python;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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

public class Fragment_ChiTiet_DapAn extends Fragment {
    private RecyclerView rvChoices;
    private Button btnTabMaDe, btnTabDapAn, btnSave; // Thêm khai báo nút Lưu
    private TextView tvCurrentMode;
    private LinearLayout layoutHeaderLabels;

    private int questionCount = 40;
    private boolean isMaDeMode = true;
    private int editingPosition = -1;

    // Chuỗi lưu dữ liệu cũ
    private String existingMaDe = "";
    private String existingDapAn = "";

    // 2 Adapter chuẩn mực mới
    private Adapter_MaDe maDeAdapter;
    private Adapter_Bubble bubbleAdapter;

    private TaoCSDL dbHelper;
    private int kyThiId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new TaoCSDL(getContext());

        if (getArguments() != null) {
            questionCount = getArguments().getInt("QUESTION_COUNT", 40);
            kyThiId = getArguments().getInt("KYTHI_ID", -1);

            // Lấy dữ liệu cũ nếu đang ở chế độ CHỈNH SỬA
            if (getArguments().containsKey("EXISTING_KEY")) {
                SavedKey existing = (SavedKey) getArguments().getSerializable("EXISTING_KEY");
                editingPosition = getArguments().getInt("EDIT_POSITION", -1);
                if (existing != null) {
                    existingMaDe = existing.getMaDe();
                    existingDapAn = existing.getDapAn();
                }
            }
        }

        maDeAdapter = new Adapter_MaDe(existingMaDe);
        bubbleAdapter = new Adapter_Bubble(existingDapAn);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // LƯU Ý MÀU ĐỎ: Nếu dòng dưới báo đỏ, bạn đổi "fragment_answer_key" thành "fragment_dap_an" nhé
        View v = inflater.inflate(R.layout.fragment_chitiet_dap_an, container, false);

        rvChoices = v.findViewById(R.id.rvChoices);
        btnTabMaDe = v.findViewById(R.id.btnTabMaDe);
        btnTabDapAn = v.findViewById(R.id.btnTabDapAn);
        tvCurrentMode = v.findViewById(R.id.tvCurrentMode);
        layoutHeaderLabels = v.findViewById(R.id.layoutHeaderLabels);

        // Ánh xạ nút Lưu ở dưới cùng màn hình
        btnSave = v.findViewById(R.id.btnSaveAnswerKey);

        setupToolbar(v);
        rvChoices.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTabMaDe.setOnClickListener(view -> { isMaDeMode = true; updateUI(); });
        btnTabDapAn.setOnClickListener(view -> {
            String chuoiMaDe = maDeAdapter.layChuoiMaDe().trim();
            
            // Nếu đang tạo mới (không phải chỉnh sửa) và mã đề đã tồn tại trong CSDL
            if (editingPosition == -1 && !chuoiMaDe.contains("X") && dbHelper.kiemTraMaDeTonTai(kyThiId, chuoiMaDe)) {
                Toast.makeText(getContext(), "Mã đề " + chuoiMaDe + " đã tồn tại trong kỳ thi này!", Toast.LENGTH_SHORT).show();
                isMaDeMode = true; // Ở lại tab Mã Đề
            } else {
                isMaDeMode = false;
            }
            updateUI();
        });

        // Sự kiện khi bấm nút Lưu
        btnSave.setOnClickListener(view -> luuKetQua());

        updateUI();
        return v;
    }

    private void updateUI() {
        if (isMaDeMode) {
            btnTabMaDe.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00CCFF")));
            btnTabDapAn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            tvCurrentMode.setText("Mã Đề");
            if (layoutHeaderLabels != null) layoutHeaderLabels.setVisibility(View.GONE);

            rvChoices.setAdapter(maDeAdapter);
        } else {
            btnTabDapAn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00CCFF")));
            btnTabMaDe.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            tvCurrentMode.setText("Đáp Án");
            if (layoutHeaderLabels != null) {
                layoutHeaderLabels.setVisibility(View.VISIBLE);
                updateHeaderLabels();
            }

            rvChoices.setAdapter(bubbleAdapter);
        }
    }

    private void luuKetQua() {
        try {
            String chuoiMaDe = "";
            if (maDeAdapter != null) {
                chuoiMaDe = maDeAdapter.layChuoiMaDe().trim();
            }

            String chuoiDapAn = "";
            if (bubbleAdapter != null) {
                chuoiDapAn = bubbleAdapter.layChuoiDapAn();
            }

            if (chuoiMaDe.isEmpty() || chuoiMaDe.contains("X")) {
                Toast.makeText(getContext(), "Lỗi: Vui lòng tô đủ 3 chữ số cho Mã Đề!", Toast.LENGTH_LONG).show();

                isMaDeMode = true;
                updateUI();
                return;
            }

            if (chuoiDapAn.contains("X")) {
                int cauBiThieu = chuoiDapAn.indexOf("X") + 1;
                Toast.makeText(getContext(), "Chưa hoàn thành! Vui lòng chọn đáp án cho câu " + cauBiThieu, Toast.LENGTH_LONG).show();
                isMaDeMode = false;
                updateUI();
                return;
            }

            Bundle result = new Bundle();
            result.putString("MA_DE", chuoiMaDe);
            result.putString("DAP_AN", chuoiDapAn);
            result.putInt("EDIT_POSITION", editingPosition);

            getParentFragmentManager().setFragmentResult("requestKey", result);
            Toast.makeText(getContext(), "Đã lưu bộ đáp án thành công!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();

        } catch (Exception e) {
            // Bọc try-catch lỡ có lỗi ngầm hệ thống thì báo lỗi ra màn hình chứ không sập app
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
    }

    private void updateHeaderLabels() {
        if (layoutHeaderLabels == null) return;
        layoutHeaderLabels.removeAllViews();

        // 1. ÉP THANH TIÊU ĐỀ THỤT LỀ 16dp Y HỆT FILE XML CỦA HÀNG ĐÁP ÁN
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        layoutHeaderLabels.setPadding(paddingPx, layoutHeaderLabels.getPaddingTop(), paddingPx, layoutHeaderLabels.getPaddingBottom());

        // 2. Ô TRỐNG BÙ CHO CỘT SỐ THỨ TỰ (Chính xác 50dp như cột số của bạn)
        TextView tvEmpty = new TextView(getContext());
        int widthInPx = (int) (50 * getResources().getDisplayMetrics().density);
        tvEmpty.setLayoutParams(new LinearLayout.LayoutParams(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutHeaderLabels.addView(tvEmpty);

        // 3. TẠO KHUNG CHỨA 4 CHỮ A B C D
        // (Bắt chước y hệt cái LinearLayout bọc 4 cái RadioButton bên XML)
        LinearLayout textContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

        // --- LƯU Ý QUAN TRỌNG CHỖ NÀY ---
        // Nếu trong file XML bạn CÓ dùng dòng 'android:layout_marginStart="20dp"' cho cụm RadioButton
        // Thì bạn mở comment 2 dòng dưới đây ra và điền đúng số 20 vào nhé để nó đẩy qua cho khớp:
        // int marginStartPx = (int) (20 * getResources().getDisplayMetrics().density);
        // containerParams.setMarginStart(marginStartPx);
        // --------------------------------

        textContainer.setLayoutParams(containerParams);
        textContainer.setOrientation(LinearLayout.HORIZONTAL);
        textContainer.setWeightSum(4);

        // 4. NHÉT CHỮ A, B, C, D VÀO TRONG KHUNG
        String[] labels = {"A", "B", "C", "D"};
        for (String label : labels) {
            TextView tv = new TextView(getContext());
            tv.setText(label);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.parseColor("#003366"));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(16);

            // Bật weight=1.0f cho từng chữ để chia đều y hệt RadioButton
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tv.setLayoutParams(p);
            textContainer.addView(tv);
        }

        // Đưa nguyên cụm khung chữ này vào thanh Tiêu đề
        layoutHeaderLabels.addView(textContainer);
    }
}
