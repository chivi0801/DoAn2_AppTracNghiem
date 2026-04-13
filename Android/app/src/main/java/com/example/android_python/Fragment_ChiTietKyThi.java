package com.example.android_python;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

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
    private Adapter_Lop lopAdapter;

    private TaoCSDL dbHelper;
    private ArrayList<Lop> danhSachLop;
    private Spinner spinnerThongKe;
    private ArrayAdapter<String> spinnerAdapter;
    private List<String> listTenLop;
    private ArrayList<Lop> listLop;
    private RecyclerView recyclerViewLop;

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
            hienThiMenuChonCachThemLop();
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
            } else if (danhSachLop.isEmpty()) {
                Toast.makeText(getContext(), "Hãy tạo thêm lớp cho kỳ thi!", Toast.LENGTH_SHORT).show();
            } else {
                hienThiDialogChonLopDeChamDiem();
            }
        });

        // --- XỬ LÝ DANH SÁCH LỚP BÊN DƯỚI ---
        danhSachLop = dbHelper.layDanhSachLopTheoKyThi(kyThiId);

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
        lopAdapter = new Adapter_Lop(danhSachLop);
        rvThongKeList.setAdapter(lopAdapter);

        // GỌI SỰ KIỆN CLICK Ở ĐÂY CHỨ ĐỪNG VIẾT HÀM BÊN NGOÀI
        lopAdapter.setOnItemClickListener(new Adapter_Lop.OnItemClickListener() {

            // SỰ KIỆN 1: BẤM VÀO LỚP CHUYỂN QUA CÁI 2 TAB
            // SỰ KIỆN 1: BẤM VÀO LỚP CHUYỂN QUA CÁI 2 TAB
            @Override
            public void onItemClick(Lop lop) {
                Fragment_item_lop fragmentItemLop = new Fragment_item_lop();

                Bundle bundle = new Bundle();
                bundle.putString("TEN_LOP", lop.getTenLop());
                bundle.putString("EXAM_NAME", examName);
                bundle.putInt("LOP_ID", lop.getLopId());
                bundle.putInt("KYTHI_ID", kyThiId); // Thêm dòng này

                fragmentItemLop.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentItemLop)
                        .addToBackStack(null)
                        .commit();
            }

            // SỰ KIỆN 2: BẤM VÀO NÚT 3 CHẤM ĐỂ GỠ LỚP (Giữ nguyên code cũ của mày)
            @Override
            public void onDeleteClick(Lop lop, int position) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Gỡ Lớp")
                        .setMessage("Bạn có chắc chắn muốn gỡ lớp " + lop.getTenLop() + " khỏi kỳ thi này không?")
                        .setPositiveButton("Gỡ", (dialog, which) -> {
                            if (dbHelper.goLopKhoiKyThi(kyThiId, lop.getLopId())) {
                                danhSachLop.remove(position);
                                lopAdapter.notifyItemRemoved(position);
                                lopAdapter.notifyItemRangeChanged(position, danhSachLop.size());

                                listTenLop.remove(position + 1);
                                spinnerAdapter.notifyDataSetChanged();

                                Toast.makeText(getContext(), "Đã gỡ lớp " + lop.getTenLop(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi gỡ lớp!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        return v; // Giữ nguyên dòng này ở cuối onCreateView
    }

    // --- MENU XỔ XUỐNG ---
    private void hienThiMenuChonCachThemLop() {
        View anchorView = getActivity().findViewById(R.id.toolbar);
        if (anchorView == null) return;

        PopupMenu popup = new PopupMenu(requireContext(), anchorView, android.view.Gravity.END);
        popup.getMenu().add(Menu.NONE, 0, Menu.NONE, "Tạo lớp mới");
        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Sử dụng lớp đã có");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                hienThiDialogThemLopMoi();
            } else if (item.getItemId() == 1) {
                hienThiDialogChonLopCu();
            }
            return true;
        });
        popup.show();
    }

    // --- CÁCH 1: TẠO LỚP MỚI HOÀN TOÀN ---
    private void hienThiDialogThemLopMoi() {
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

            if (!tenLop.isEmpty() && !nienKhoa.isEmpty() && gvId != -1 && kyThiId != -1) {
                // 1. Lưu lớp vào bảng Lớp -> Lấy ra ID của lớp vừa tạo
                long newLopId = dbHelper.themLop(gvId, tenLop, nienKhoa);

                if (newLopId != -1) {
                    // 2. Liên kết Lớp đó với Kỳ Thi hiện tại trong bảng trung gian
                    dbHelper.themKyThiLop(kyThiId, (int) newLopId);
                    capNhatDanhSachLop();
                    Toast.makeText(getContext(), "Tạo lớp mới thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm lớp", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null).show();
    }

    // --- CÁCH 2: CHỌN LỚP ĐÃ CÓ CỦA GIÁO VIÊN ---
    private void hienThiDialogChonLopCu() {
        if (gvId == -1) return;

        ArrayList<Lop> danhSachLopCu = dbHelper.layDanhSachLopCuaGV(gvId);

        if (danhSachLopCu.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa tạo lớp nào trước đây!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] arrLopCu = new String[danhSachLopCu.size()];
        for (int i = 0; i < danhSachLopCu.size(); i++) {
            arrLopCu[i] = danhSachLopCu.get(i).getTenLop() + " (" + danhSachLopCu.get(i).getNienKhoa() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn lớp đã có");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(50, 40, 50, 40);

        Spinner spinnerLopCu = new Spinner(getContext());
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrLopCu);
        spinnerLopCu.setAdapter(spinAdapter);
        layout.addView(spinnerLopCu);

        builder.setView(layout);
        builder.setPositiveButton("Thêm vào kỳ thi", (dialog, which) -> {
            int pos = spinnerLopCu.getSelectedItemPosition();
            Lop lopDuocChon = danhSachLopCu.get(pos);

            // Vì lớp đã có sẵn ID, ta chỉ cần nhét nó vào bảng trung gian KyThi_Lop
            if (kyThiId != -1 && dbHelper.themKyThiLop(kyThiId, lopDuocChon.getLopId())) {
                capNhatDanhSachLop();
                Toast.makeText(getContext(), "Đã thêm lớp " + lopDuocChon.getTenLop(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Lớp này có thể đã được thêm rồi!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null).show();
    }

    // --- HÀM CẬP NHẬT GIAO DIỆN CHUNG ---
    private void capNhatDanhSachLop() {
        danhSachLop.clear();
        // Lấy danh sách lớp thuộc Kỳ thi thông qua bảng trung gian (Hàm bạn vừa thêm)
        danhSachLop.addAll(dbHelper.layDanhSachLopTheoKyThi(kyThiId));
        lopAdapter.notifyDataSetChanged();

        listTenLop.clear();
        listTenLop.add("Thống kê");
        for (Lop lop : danhSachLop) {
            listTenLop.add(lop.getTenLop() + " (" + lop.getNienKhoa() + ")");
        }
        spinnerAdapter.notifyDataSetChanged();
        spinnerThongKe.setSelection(listTenLop.size() - 1);
    }

    private void hienThiDialogChonLopDeChamDiem() {
        String[] arrTenLop = new String[danhSachLop.size()];
        for (int i = 0; i < danhSachLop.size(); i++) {
            arrTenLop[i] = danhSachLop.get(i).getTenLop() + " (" + danhSachLop.get(i).getNienKhoa() + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn lớp để chấm điểm")
                .setItems(arrTenLop, (dialog, which) -> {
                    Lop lopSelected = danhSachLop.get(which);
                    openGradeFragment(lopSelected.getLopId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openGradeFragment(int lopId) {
        Intent intent = new Intent(getActivity(), Activity_Camera.class);
        intent.putExtra("EXAM_NAME", examName);
        intent.putExtra("KYTHI_ID", kyThiId);
        intent.putExtra("LOP_ID", lopId);
        startActivity(intent);
    }

    private void setupToolbar(View v) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            // HIỆN nút Quay về và ẨN icon Menu
            View iconMenu = activity.findViewById(R.id.icon_Menu);
            if (iconMenu != null) iconMenu.setVisibility(View.GONE); // Ẩn nút Menu


            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay về
                activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> {
                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                });
            }
        }
        TextView title = getActivity().findViewById(R.id.toolbar_title);
        if (title != null) title.setText(examName);
    }
    private void setupRecyclerView() {
        // SỬA LỖI 1: Khởi tạo adapter chỉ truyền vào listLop (bỏ getContext() đi)
        lopAdapter = new Adapter_Lop(listLop);

        lopAdapter.setOnItemClickListener(new Adapter_Lop.OnItemClickListener() {

            public void onItemClick(Lop lop) {
                Toast.makeText(getContext(), "Đã click vào lớp: " + lop.getTenLop(), Toast.LENGTH_SHORT).show();
                Fragment_item_lop fragmentItemLop = new Fragment_item_lop();

                Bundle bundle = new Bundle();
                bundle.putString("TEN_LOP", lop.getTenLop());
                bundle.putString("EXAM_NAME", examName);
                fragmentItemLop.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.drawer_layout, fragmentItemLop) // Nhớ đổi ID này cho đúng với layout của bạn nhé
                        .addToBackStack(null)
                        .commit();
            }

            // SỬA LỖI 2: Phải thêm hàm onDeleteClick này vào thì mới không bị báo lỗi
            // Sự kiện 2: Click vào nút 3 chấm để xóa
            @Override
            public void onDeleteClick(Lop lop, int position) {
                // Xóa phần tử khỏi danh sách
                listLop.remove(position);
                // Báo cho Adapter biết vị trí đã bị xóa để cập nhật giao diện
                lopAdapter.notifyItemRemoved(position);

                // (Tuỳ chọn) Bạn có thể gọi thêm code xóa trong Database ở đây
            }
        });

        recyclerViewLop.setAdapter(lopAdapter);
    }
}