package com.example.android_python;

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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Ds_MaDe extends Fragment {
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;
    private List<SavedKey> savedKeyList = new ArrayList<>();

    private int questionCount;
    private int kyThiId = -1;
    private TaoCSDL dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new TaoCSDL(getContext());

        if (getArguments() != null) {
            questionCount = getArguments().getInt("QUESTION_COUNT", 40);
            kyThiId = getArguments().getInt("KYTHI_ID", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_made, container, false);

        setupToolbar(view);

        // 1. RecyclerView lấy data trực tiếp từ CSDL
        loadDataFromDatabase();

        rvSavedKeys = view.findViewById(R.id.rvExams);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SavedKeyAdapter(savedKeyList, new SavedKeyAdapter.OnMaDeActionListener() {
            @Override
            public void onItemClick(SavedKey item, int position) {
                openEditAnswerKey(item, position);
            }

            @Override
            public void onDeleteClick(SavedKey item, int position) {
                confirmDelete(item, position);
            }
        });
        rvSavedKeys.setAdapter(adapter);

        // 2. Nút thêm (+) để mở trang tạo mã đề mới
        view.findViewById(R.id.fabAdd).setOnClickListener(v -> openEditAnswerKey(null, -1));

        // 3. LẮNG NGHE KẾT QUẢ VÀ LƯU VÀO CSDL
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String maDe = bundle.getString("MA_DE");
            String dapAn = bundle.getString("DAP_AN");
            int editPos = bundle.getInt("EDIT_POSITION", -1);

            if (maDe != null && dapAn != null && kyThiId != -1) {
                if (editPos != -1) {
                    // TRƯỜNG HỢP 1: CHỈNH SỬA MÃ ĐỀ (UPDATE)
                    SavedKey oldKey = savedKeyList.get(editPos);
                    dbHelper.suaMaDe(kyThiId, oldKey.getMaDe(), maDe, dapAn);

                    // Cập nhật lại giao diện
                    loadDataFromDatabase();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã cập nhật mã đề!", Toast.LENGTH_SHORT).show();

                } else {
                    // TRƯỜNG HỢP 2: TẠO MỚI MÃ ĐỀ (INSERT)
                    // Gọi hàm kiểm tra trùng lặp trước khi thêm
                    if (dbHelper.kiemTraMaDeTonTai(kyThiId, maDe)) {
                        // Trùng mã đề -> Báo lỗi và KHÔNG LƯU
                        Toast.makeText(getContext(), "Lỗi: Mã đề " + maDe + " đã tồn tại!", Toast.LENGTH_LONG).show();
                    } else {
                        // Không trùng -> Tiến hành lưu
                        dbHelper.themBoDapAn( maDe,kyThiId, dapAn);

                        // Chỉ cập nhật lại giao diện khi lưu thành công
                        loadDataFromDatabase();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Tạo mã đề thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    // Hàm đọc dữ liệu từ SQLite
    private void loadDataFromDatabase() {
        savedKeyList.clear();
        if (kyThiId != -1) {
            // Gọi hàm truy vấn từ file TaoCSDL
            savedKeyList.addAll(dbHelper.layDanhSachMaDe(kyThiId));
        }
    }

    private void confirmDelete(SavedKey item, int position) {
        new AlertDialog.Builder(requireContext())
                // 1. THÊM TIÊU ĐỀ
                .setTitle("Xác nhận xóa mã đề")

                // 2. THÊM NỘI DUNG (Cố tình nhét luôn cái tên mã đề vào cho chuyên nghiệp)
                .setMessage("Bạn có chắc chắn muốn xóa mã đề " + item.getMaDe() + " không? Dữ liệu này không thể khôi phục.")

                // Phần nút bấm giữ nguyên
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int actualPosition = savedKeyList.indexOf(item);
                    if (actualPosition != -1) {
                        dbHelper.xoaMaDe(kyThiId, item.getMaDe());
                        savedKeyList.remove(actualPosition);
                        adapter.notifyItemRemoved(actualPosition);
                        adapter.notifyItemRangeChanged(actualPosition, savedKeyList.size());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
        b.putInt("KYTHI_ID", kyThiId); // TRUYỀN THÊM KYTHI_ID
        if (item != null) {
            b.putSerializable("EXISTING_KEY", item);
            b.putInt("EDIT_POSITION", pos);
        }
        fragment.setArguments(b);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}