package com.example.android_python;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Fragment_Lop extends Fragment {
    private RecyclerView rvLop;
    private Adapter_Lop lopAdapter;
    private ArrayList<Lop> listLop;
    private TaoCSDL dbHelper;
    private int gvId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        dbHelper = new TaoCSDL(getContext());
        gvId = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE).getInt("GV_ID", -1);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem addItem = menu.add(Menu.NONE, 1002, Menu.NONE, "Thêm Lớp");
        addItem.setIcon(R.drawable.group_add);
        addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        addItem.setOnMenuItemClickListener(item -> {
            hienThiDialogThemLopMoi();
            return true;
        });
        super.onCreateOptionsMenu(menu, inflater);
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

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String tenLop = inputTenLop.getText().toString().trim();
            String nienKhoa = inputNienKhoa.getText().toString().trim();

            if (!tenLop.isEmpty()) {
                long newLopId = dbHelper.themLop(gvId, tenLop, nienKhoa);
                if (newLopId != -1) {
                    loadData(); // Tải lại danh sách sau khi thêm
                    Toast.makeText(getContext(), "Đã tạo lớp " + tenLop, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên lớp!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ds_kythi, container, false);

        rvLop = view.findViewById(R.id.rvExams);
        rvLop.setLayoutManager(new LinearLayoutManager(getContext()));

        if (view.findViewById(R.id.fabAdd) != null) {
            view.findViewById(R.id.fabAdd).setVisibility(View.GONE);
        }

        setupToolbar();
        loadData();

        return view;
    }


    private void loadData() {
        listLop = dbHelper.layDanhSachLopDuyNhat(gvId);

        lopAdapter = new Adapter_Lop(listLop);

        lopAdapter.setOnItemClickListener(new Adapter_Lop.OnItemClickListener() {
            @Override
            public void onItemClick(Lop lop) {
                // Chuyển sang danh sách thí sinh và thêm vào BackStack để có thể quay lại
                Fragment_DanhSachLop fragment = new Fragment_DanhSachLop();
                Bundle bundle = new Bundle();
                bundle.putInt("LOP_ID", lop.getLopId());
                bundle.putString("TEN_LOP", lop.getTenLop());
                fragment.setArguments(bundle);

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragmentWithBackStack(fragment);
                }
            }

            @Override
            public void onDeleteClick(Lop lop, int position) {
                confirmDelete(lop, position);
            }

            @Override
            public void onEditClick(Lop lop, int position) {
                showEditLopDialog(lop, position);
            }
        });

        rvLop.setAdapter(lopAdapter);
    }

    private void showEditLopDialog(Lop lop, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa lớp");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText inputTenLop = new EditText(getContext());
        inputTenLop.setHint("Tên lớp");
        inputTenLop.setText(lop.getTenLop());
        layout.addView(inputTenLop);

        final EditText inputNienKhoa = new EditText(getContext());
        inputNienKhoa.setHint("Niên khóa");
        inputNienKhoa.setText(lop.getNienKhoa());
        layout.addView(inputNienKhoa);

        builder.setView(layout);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String tenLop = inputTenLop.getText().toString().trim();
            String nienKhoa = inputNienKhoa.getText().toString().trim();

            if (!tenLop.isEmpty()) {
                if (dbHelper.capNhatLop(lop.getLopId(), tenLop, nienKhoa)) {
                    lop.setTenLop(tenLop);
                    lop.setNienKhoa(nienKhoa);
                    lopAdapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Đã cập nhật lớp " + tenLop, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên lớp!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void confirmDelete(Lop lop, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lớp vĩnh viễn")
                .setMessage("Bạn có chắc chắn muốn xóa lớp " + lop.getTenLop() + "?\nHành động này sẽ xóa vĩnh viễn lớp, toàn bộ thí sinh và các bài thi liên quan trong hệ thống.")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    if (dbHelper.xoaLop(lop.getLopId())) {
                        listLop.remove(position);
                        lopAdapter.notifyItemRemoved(position);
                        lopAdapter.notifyItemRangeChanged(position, listLop.size());
                        Toast.makeText(getContext(), "Đã xóa lớp khỏi hệ thống", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi xóa dữ liệu trong CSDL!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            TextView title = activity.findViewById(R.id.toolbar_title);
            if (title != null) title.setText("Danh Sách Lớp");
            
            // Hiện icon quay lại nếu cần (nếu fragment_container dùng chung)
            androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                // Ẩn icon Menu (Hamburger) và hiện nút Quay lại nếu là màn hình con
                // Ở đây là màn hình chính của Lớp nên có thể tùy chọn hiện icon Menu
                activity.invalidateOptionsMenu(); 
            }
        }
    }
}