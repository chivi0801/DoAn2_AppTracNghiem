package com.example.android_python;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

public class Fragment_ChiTietKyThi extends Fragment {
    private String examName;
    private int kyThiId = -1;
    private int questionCount;
    private int gvId = -1;

    private RecyclerView rvThongKeList;
    private LopAdapter lopAdapter;

    private TaoCSDL dbHelper;
    private ArrayList<Lop> danhSachLop;
    private Spinner spinnerThongKe;
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> listTenLop;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dbHelper = new TaoCSDL(getContext());

        // Lấy gvId từ SharedPreferences
        android.content.SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
        gvId = sharedPreferences.getInt("GV_ID", -1);

        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME");
            kyThiId = getArguments().getInt("KYTHI_ID", -1);
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem addItem = menu.add(Menu.NONE, 1001, Menu.NONE, "Thêm Lớp");
        addItem.setIcon(R.drawable.group_add);
        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

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

        setupToolbar(v);

        // Nút Đáp án: Chuyển qua trang Danh sách Mã Đề
        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> {
            Fragment_Ds_MaDe fragment = new Fragment_Ds_MaDe();
            Bundle b = new Bundle();
            b.putInt("KYTHI_ID", kyThiId); // BẮT BUỘC PHẢI CÓ DÒNG NÀY ĐỂ BÊN KIA NHẬN DIỆN
            b.putInt("QUESTION_COUNT", questionCount);
            b.putString("EXAM_NAME", examName);
            fragment.setArguments(b);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Nút Chấm Điểm
        v.findViewById(R.id.btnGrade).setOnClickListener(view -> {
            // Kiểm tra trực tiếp từ CSDL xem kỳ thi này đã có mã đề nào chưa
            List<SavedKey> checkList = dbHelper.layDanhSachMaDe(kyThiId);
            if (checkList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng thêm Đáp Án trước khi chấm điểm!", Toast.LENGTH_SHORT).show();
            } else {
                openGradeFragment();
            }
        });

        // --- XỬ LÝ DANH SÁCH LỚP BÊN DƯỚI ---
        danhSachLop = dbHelper.layDanhSachLop(gvId);

        spinnerThongKe = v.findViewById(R.id.spinnerThongKe);
        listTenLop = new ArrayList<>();
        listTenLop.add("Thống kê");

        for (Lop lop : danhSachLop) {
            listTenLop.add(lop.getTenLop() + " (" + lop.getNienKhoa() + ")");
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listTenLop);
        spinnerThongKe.setAdapter(spinnerAdapter);

        rvThongKeList = v.findViewById(R.id.rvThongKeList);
        rvThongKeList.setLayoutManager(new LinearLayoutManager(getContext()));
        lopAdapter = new LopAdapter(danhSachLop);
        rvThongKeList.setAdapter(lopAdapter);

        lopAdapter.setOnItemClickListener((lop, position) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa Lớp")
                    .setMessage("Bạn có chắc chắn muốn xóa lớp " + lop.getTenLop() + " không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (dbHelper.xoaLop(lop.getLopId())) {
                            danhSachLop.remove(position);
                            lopAdapter.notifyItemRemoved(position);
                            lopAdapter.notifyItemRangeChanged(position, danhSachLop.size());

                            listTenLop.remove(position + 1);
                            spinnerAdapter.notifyDataSetChanged();

                            Toast.makeText(getContext(), "Đã xóa lớp " + lop.getTenLop(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return v;
    }

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
                if (gvId == -1) {
                    Toast.makeText(getContext(), "Lỗi xác thực người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isInserted = dbHelper.themLop(gvId, tenLop, nienKhoa);
                if (isInserted) {
                    Toast.makeText(getContext(), "Thêm lớp thành công!", Toast.LENGTH_SHORT).show();
                    danhSachLop.clear();
                    danhSachLop.addAll(dbHelper.layDanhSachLop(gvId));

                    listTenLop.clear();
                    listTenLop.add("Thống kê");
                    for (Lop lop : danhSachLop) {
                        listTenLop.add(lop.getTenLop() + " (" + lop.getNienKhoa() + ")");
                    }
                    spinnerAdapter.notifyDataSetChanged();
                    spinnerThongKe.setSelection(listTenLop.size() - 1);

                    lopAdapter.notifyDataSetChanged();
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
        intent.putExtra("KYTHI_ID", kyThiId);
        startActivity(intent);
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