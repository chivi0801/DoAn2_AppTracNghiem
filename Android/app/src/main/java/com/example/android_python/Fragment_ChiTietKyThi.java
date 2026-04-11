package com.example.android_python;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_ChiTietKyThi extends Fragment {
    private String examName;
    private int questionCount;
    private RecyclerView rvSavedKeys;
    private SavedKeyAdapter adapter;
    private static List<SavedKey> savedKeyList = new ArrayList<>();

    // Các biến cho Thống kê Lớp
    private TaoCSDL dbHelper;
    private RecyclerView rvThongKeList;
    private LopAdapter lopAdapter;
    private ArrayList<Lop> danhSachLop;
    private ImageView ivArrow; // Khai báo global để dùng chung

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BẬT TÍNH NĂNG MENU CHO FRAGMENT (Để nhét nút + lên Toolbar)
        setHasOptionsMenu(true);

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

    // --- TẠO NÚT (+) TRÊN TOOLBAR MÀU XANH ---
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Tạo một item menu mới bằng code
        MenuItem addItem = menu.add(Menu.NONE, 1001, Menu.NONE, "Thêm Lớp");
        addItem.setIcon(R.drawable.group_add);
        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // --- BẮT SỰ KIỆN KHI BẤM VÀO NÚT (+) TRÊN TOOLBAR ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1001) {
            hienThiDialogThemLop();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chitiet_kythi, container, false);

        rvSavedKeys = v.findViewById(R.id.layoutSavedKeys);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            openAnswerKey(savedKeyList.get(position), position);
        });
        rvSavedKeys.setAdapter(adapter);

        setupSwipeToDelete();
        setupToolbar(v);

        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> openAnswerKey(null, -1));

        v.findViewById(R.id.btnGrade).setOnClickListener(view -> {
            if (savedKeyList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập Đáp Án trước khi chấm điểm!", Toast.LENGTH_SHORT).show();
            } else {
                openGradeFragment();
            }
        });

        // Xử lý Thống kê Lớp
        dbHelper = new TaoCSDL(getContext());
        danhSachLop = dbHelper.layDanhSachLop();

        rvThongKeList = v.findViewById(R.id.rvThongKeList);
        rvThongKeList.setLayoutManager(new LinearLayoutManager(getContext()));
        lopAdapter = new LopAdapter(danhSachLop);
        rvThongKeList.setAdapter(lopAdapter);

        lopAdapter.setOnItemClickListener((lop, position) -> {
            // Hiện hộp thoại (Dialog) hỏi lại cho chắc chắn
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa Lớp")
                    .setMessage("Bạn có chắc chắn muốn xóa lớp " + lop.getTenLop() + " không? Hành động này không thể hoàn tác.")
                    .setPositiveButton("Xóa", (dialog, which) -> {

                        // 1. Xóa trong SQLite Database
                        boolean isDeleted = dbHelper.xoaLop(lop.getLopId());

                        if (isDeleted) {
                            // 2. Xóa khỏi danh sách hiển thị
                            danhSachLop.remove(position);

                            // 3. Báo cho Adapter biết để vẽ lại UI
                            lopAdapter.notifyItemRemoved(position);
                            lopAdapter.notifyItemRangeChanged(position, danhSachLop.size());

                            Toast.makeText(getContext(), "Đã xóa lớp " + lop.getTenLop(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi xóa lớp!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        View cardStatsHeader = v.findViewById(R.id.cardStatsHeader);
        ivArrow = v.findViewById(R.id.ivArrow); // Ánh xạ mũi tên

        // Xổ xuống danh sách
        cardStatsHeader.setOnClickListener(view -> {
            if (rvThongKeList.getVisibility() == View.VISIBLE) {
                rvThongKeList.setVisibility(View.GONE);
                ivArrow.animate().rotation(0f).start();
            } else {
                rvThongKeList.setVisibility(View.VISIBLE);
                ivArrow.animate().rotation(180f).start();
            }
        });

        return v;
    }

    // --- HÀM HIỂN THỊ DIALOG NHẬP LỚP ---
    private void hienThiDialogThemLop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Lớp Mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText edtTenLop = new EditText(getContext());
        edtTenLop.setHint("Nhập tên lớp (VD: 12A1)");
        layout.addView(edtTenLop);

        final EditText edtNienKhoa = new EditText(getContext());
        edtNienKhoa.setHint("Nhập niên khóa (VD: 2023)");
        layout.addView(edtNienKhoa);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String tenLop = edtTenLop.getText().toString().trim();
            String nienKhoa = edtNienKhoa.getText().toString().trim();

            if (!tenLop.isEmpty() && !nienKhoa.isEmpty()) {
                boolean isInserted = dbHelper.themLop(tenLop, nienKhoa);
                if (isInserted) {
                    Toast.makeText(getContext(), "Thêm lớp thành công!", Toast.LENGTH_SHORT).show();

                    // Làm mới danh sách
                    danhSachLop.clear();
                    danhSachLop.addAll(dbHelper.layDanhSachLop());
                    lopAdapter.notifyDataSetChanged();

                    // Tự động mở danh sách xổ xuống
                    rvThongKeList.setVisibility(View.VISIBLE);
                    if (ivArrow != null) ivArrow.setRotation(180f);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm lớp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openGradeFragment() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        intent.putExtra("EXAM_NAME", examName);
        startActivity(intent);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                savedKeyList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa bộ mã đề", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSavedKeys);
    }

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