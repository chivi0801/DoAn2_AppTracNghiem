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

    // --- BIẾN CHO GIAO DIỆN BIỂU ĐỒ ---
    private LinearLayout layoutThongKeTuLam;
    private View barYeu, barTB, barKha, barGioi;
    private TextView tvCountYeu, tvCountTB, tvCountKha, tvCountGioi;

    private View spaceYeu, spaceTB, spaceKha, spaceGioi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dbHelper = new TaoCSDL(getContext());

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
        
        // Bắt sự kiện click trực tiếp trên MenuItem để đảm bảo phản hồi nhanh và chính xác
        addItem.setOnMenuItemClickListener(item -> {
            hienThiMenuChonCachThemLop();
            return true;
        });
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

        // Ánh xạ các View của Biểu đồ
        layoutThongKeTuLam = v.findViewById(R.id.layoutThongKeTuLam);
        barYeu = v.findViewById(R.id.barYeu);

        // Thêm 4 dòng này
        spaceYeu = v.findViewById(R.id.spaceYeu);
        spaceTB = v.findViewById(R.id.spaceTB);
        spaceKha = v.findViewById(R.id.spaceKha);
        spaceGioi = v.findViewById(R.id.spaceGioi);
        barTB = v.findViewById(R.id.barTB);
        barKha = v.findViewById(R.id.barKha);
        barGioi = v.findViewById(R.id.barGioi);
        tvCountYeu = v.findViewById(R.id.tvCountYeu);
        tvCountTB = v.findViewById(R.id.tvCountTB);
        tvCountKha = v.findViewById(R.id.tvCountKha);
        tvCountGioi = v.findViewById(R.id.tvCountGioi);

        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> {
            Fragment_Ds_MaDe fragment = new Fragment_Ds_MaDe();
            Bundle b = new Bundle();
            b.putInt("KYTHI_ID", kyThiId);
            b.putInt("QUESTION_COUNT", questionCount);
            b.putString("EXAM_NAME", examName);
            fragment.setArguments(b);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        v.findViewById(R.id.btnGrade).setOnClickListener(view -> {
            List<SavedKey> checkList = dbHelper.layDanhSachMaDe(kyThiId);
            if (checkList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng thêm Đáp Án trước khi chấm điểm!", Toast.LENGTH_SHORT).show();
            } else if (danhSachLop.isEmpty()) {
                Toast.makeText(getContext(), "Hãy tạo thêm lớp cho kỳ thi!", Toast.LENGTH_SHORT).show();
            } else {
                hienThiDialogChonLopDeChamDiem();
            }
        });

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

        // XỬ LÝ SỰ KIỆN KHI CHỌN SPINNER
        spinnerThongKe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Chọn "Thống kê" -> Ẩn biểu đồ, hiện danh sách
                    layoutThongKeTuLam.setVisibility(View.GONE);
                    rvThongKeList.setVisibility(View.VISIBLE);
                } else {
                    // Chọn Lớp -> Hiện biểu đồ, ẩn danh sách
                    rvThongKeList.setVisibility(View.GONE);
                    layoutThongKeTuLam.setVisibility(View.VISIBLE);

                    Lop lopDuocChon = danhSachLop.get(position - 1);
                    veThongKeDiemThuCong(lopDuocChon.getLopId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        lopAdapter.setOnItemClickListener(new Adapter_Lop.OnItemClickListener() {
            @Override
            public void onItemClick(Lop lop) {
                Fragment_item_lop fragmentItemLop = new Fragment_item_lop();
                Bundle bundle = new Bundle();
                bundle.putString("TEN_LOP", lop.getTenLop());
                bundle.putString("EXAM_NAME", examName);
                bundle.putInt("LOP_ID", lop.getLopId());
                fragmentItemLop.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentItemLop)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(Lop lop, int position) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Gỡ lớp khỏi kỳ thi")
                        .setMessage("Bạn có chắc chắn muốn gỡ lớp " + lop.getTenLop() + " khỏi kỳ thi này không?\nDữ liệu của lớp vẫn sẽ còn trong danh sách Lớp học.")
                        .setPositiveButton("Gỡ bỏ", (dialog, which) -> {
                            if (dbHelper.goLopKhoiKyThi(kyThiId, lop.getLopId())) {
                                danhSachLop.remove(position);
                                lopAdapter.notifyItemRemoved(position);
                                lopAdapter.notifyItemRangeChanged(position, danhSachLop.size());
                                
                                // Cập nhật Spinner
                                if (listTenLop.size() > position + 1) {
                                    listTenLop.remove(position + 1);
                                    spinnerAdapter.notifyDataSetChanged();
                                }
                                
                                Toast.makeText(getContext(), "Đã gỡ lớp " + lop.getTenLop() + " khỏi kỳ thi", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi gỡ lớp!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        return v;
    }

    private void veThongKeDiemThuCong(int lopId) {
        // Lấy dữ liệu thực tế từ CSDL
        ArrayList<Double> listDiem = dbHelper.layDanhSachDiemTheoLop(kyThiId, lopId);

        int duoi4 = 0;
        int tu4den6 = 0;
        int tu6den8 = 0;
        int tren8 = 0;

        for (Double diem : listDiem) {
            if (diem < 4) duoi4++;
            else if (diem < 6) tu4den6++;
            else if (diem < 8) tu6den8++;
            else tren8++;
        }

        int tongSoHocSinh = listDiem.size();

        if (tongSoHocSinh == 0) {
            setBarHeight(spaceYeu, barYeu, 0);
            setBarHeight(spaceTB, barTB, 0);
            setBarHeight(spaceKha, barKha, 0);
            setBarHeight(spaceGioi, barGioi, 0);
            tvCountYeu.setText("0"); tvCountTB.setText("0"); tvCountKha.setText("0"); tvCountGioi.setText("0");
            return;
        }

        tvCountYeu.setText(String.valueOf(duoi4));
        tvCountTB.setText(String.valueOf(tu4den6));
        tvCountKha.setText(String.valueOf(tu6den8));
        tvCountGioi.setText(String.valueOf(tren8));

        float ptYeu = ((float) duoi4 / tongSoHocSinh) * 100;
        float ptTB = ((float) tu4den6 / tongSoHocSinh) * 100;
        float ptKha = ((float) tu6den8 / tongSoHocSinh) * 100;
        float ptGioi = ((float) tren8 / tongSoHocSinh) * 100;

        // GỌI HÀM MỚI Ở ĐÂY
        setBarHeight(spaceYeu, barYeu, ptYeu);
        setBarHeight(spaceTB, barTB, ptTB);
        setBarHeight(spaceKha, barKha, ptKha);
        setBarHeight(spaceGioi, barGioi, ptGioi);
    }

    // HÀM MỚI (Thay thế cho setBarWidth cũ)
    private void setBarHeight(View space, View bar, float percent) {
        float barWeight = percent > 0 ? Math.max(percent, 1f) : 0; // Đảm bảo nếu có người thì hiện 1 vạch mỏng
        float spaceWeight = 100f - barWeight; // Phần trống còn lại

        // Cập nhật phần Trống (Space)
        LinearLayout.LayoutParams spaceParams = (LinearLayout.LayoutParams) space.getLayoutParams();
        spaceParams.weight = spaceWeight;
        space.setLayoutParams(spaceParams);

        // Cập nhật phần Màu (Bar)
        LinearLayout.LayoutParams barParams = (LinearLayout.LayoutParams) bar.getLayoutParams();
        barParams.weight = barWeight;
        bar.setLayoutParams(barParams);
    }
    // ==========================================

    private void hienThiMenuChonCachThemLop() {
        // Lấy view của Toolbar để làm điểm neo (anchor) cho Menu Dropdown
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        View anchor = toolbar != null ? toolbar.findViewById(1001) : null;
        if (anchor == null) anchor = toolbar; // Fallback nếu không tìm thấy view cụ thể của icon

        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 0, 0, "Tạo lớp mới");
        popup.getMenu().add(0, 1, 0, "Chọn lớp có sẵn");

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

    private void hienThiDialogThemLopMoi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tạo lớp mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText inputTenLop = new EditText(getContext());
        inputTenLop.setHint("Tên lớp (VD: CNTT K15)");
        layout.addView(inputTenLop);

        final EditText inputNienKhoa = new EditText(getContext());
        inputNienKhoa.setHint("Niên khóa (VD: 2023-2027)");
        layout.addView(inputNienKhoa);

        builder.setView(layout);

        builder.setPositiveButton("Tạo & Thêm", (dialog, which) -> {
            String tenLop = inputTenLop.getText().toString().trim();
            String nienKhoa = inputNienKhoa.getText().toString().trim();

            if (!tenLop.isEmpty()) {
                long newLopId = dbHelper.themLop(gvId, tenLop, nienKhoa);
                if (newLopId != -1) {
                    dbHelper.themKyThiLop(kyThiId, (int) newLopId);
                    capNhatDanhSachLop();
                    Toast.makeText(getContext(), "Đã thêm lớp " + tenLop, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên lớp!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void hienThiDialogChonLopCu() {
        ArrayList<Lop> dsLopCuaGV = dbHelper.layDanhSachLopCuaGV(gvId);
        ArrayList<Lop> availableLops = new ArrayList<>();

        for (Lop l : dsLopCuaGV) {
            boolean isAdded = false;
            for (Lop added : danhSachLop) {
                if (l.getLopId() == added.getLopId()) {
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) availableLops.add(l);
        }

        if (availableLops.isEmpty()) {
            Toast.makeText(getContext(), "Không còn lớp nào để chọn!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[availableLops.size()];
        for (int i = 0; i < availableLops.size(); i++) {
            names[i] = availableLops.get(i).getTenLop() + " (" + availableLops.get(i).getNienKhoa() + ")";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn lớp có sẵn")
                .setItems(names, (dialog, which) -> {
                    Lop selected = availableLops.get(which);
                    dbHelper.themKyThiLop(kyThiId, selected.getLopId());
                    capNhatDanhSachLop();
                    Toast.makeText(getContext(), "Đã thêm lớp " + selected.getTenLop(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void capNhatDanhSachLop() {
        danhSachLop.clear();
        danhSachLop.addAll(dbHelper.layDanhSachLopTheoKyThi(kyThiId));
        lopAdapter.notifyDataSetChanged();

        listTenLop.clear();
        listTenLop.add("Thống kê");
        for (Lop lop : danhSachLop) {
            listTenLop.add(lop.getTenLop() + " (" + lop.getNienKhoa() + ")");
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    private void hienThiDialogChonLopDeChamDiem() {
        if (danhSachLop.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm lớp trước!", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[danhSachLop.size()];
        for (int i = 0; i < danhSachLop.size(); i++) {
            names[i] = danhSachLop.get(i).getTenLop();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn lớp để chấm điểm")
                .setItems(names, (dialog, which) -> {
                    openGradeFragment(danhSachLop.get(which).getLopId());
                })
                .show();
    }

    private void openGradeFragment(int lopId) {
        Intent intent = new Intent(getContext(), Activity_Camera.class);
        intent.putExtra("KYTHI_ID", kyThiId);
        intent.putExtra("LOP_ID", lopId);
        intent.putExtra("EXAM_NAME", examName);
        intent.putExtra("QUESTION_COUNT", questionCount);
        startActivity(intent);
    }

    private void setupToolbar(View v) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                // Cập nhật tiêu đề trực tiếp vào TextView trong Toolbar
                TextView title = toolbar.findViewById(R.id.toolbar_title);
                if (title != null) {
                    title.setText(examName != null ? examName : "Chi tiết");
                }

                // Ẩn icon Menu (Hamburger) và hiện nút Quay lại
                View iconMenu = toolbar.findViewById(R.id.icon_Menu);
                if (iconMenu != null) iconMenu.setVisibility(View.GONE);

                if (activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
                toolbar.setNavigationIcon(R.drawable.ic_back_white);
                toolbar.setNavigationOnClickListener(view -> {
                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                });
                
                // Quan trọng: Làm mới Menu để hiển thị nút "Thêm Lớp"
                activity.invalidateOptionsMenu();
            }
        }
    }
}