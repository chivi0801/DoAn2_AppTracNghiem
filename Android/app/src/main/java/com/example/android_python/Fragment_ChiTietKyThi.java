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
import android.widget.Button;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_ChiTietKyThi extends Fragment {
    private String examName;
    private int kyThiId = -1;
    private int questionCount;
    private RecyclerView rvSavedKeys;
    private RecyclerView rvThongKeList;
    private LopAdapter lopAdapter;
    private SavedKeyAdapter adapter;
    public static List<SavedKey> savedKeyList = new ArrayList<>();

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

        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME");
            kyThiId = getArguments().getInt("KYTHI_ID", -1);
            questionCount = getArguments().getInt("QUESTION_COUNT", 30);
        }

        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            String maDe = bundle.getString("MA_DE");
            String dapAn = bundle.getString("DAP_AN");
            int editPos = bundle.getInt("EDIT_POSITION", -1);
            if (maDe != null && dapAn != null && kyThiId != -1) {
                if (editPos != -1 && editPos < savedKeyList.size()) {
                    SavedKey oldKey = savedKeyList.get(editPos);
                    dbHelper.suaMaDe(kyThiId, oldKey.getMaDe(), maDe, dapAn);
                    savedKeyList.set(editPos, new SavedKey(maDe, dapAn));
                } else {
                    dbHelper.themMaDe(kyThiId, maDe, dapAn);
                    savedKeyList.add(new SavedKey(maDe, dapAn));
                }
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
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

        savedKeyList.clear();
        if (kyThiId != -1) {
            savedKeyList.addAll(dbHelper.layDanhSachMaDe(kyThiId));
        }

        rvSavedKeys = v.findViewById(R.id.layoutSavedKeys);
        rvSavedKeys.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavedKeyAdapter(savedKeyList, position -> {
            openAnswerKey(savedKeyList.get(position), position);
        });
        rvSavedKeys.setAdapter(adapter);

        setupSwipeToDelete();
        setupToolbar(v);

        //nút đáp án
        // Sửa nút Đáp án: Chuyển qua trang Fragment_Ds_MaDe
        v.findViewById(R.id.cardAnswers).setOnClickListener(view -> {
            Fragment_Ds_MaDe fragment = new Fragment_Ds_MaDe();
            Bundle b = new Bundle();
            b.putInt("QUESTION_COUNT", questionCount);
            b.putString("EXAM_NAME", examName);
            fragment.setArguments(b);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        v.findViewById(R.id.btnGrade).setOnClickListener(view -> {
            if (savedKeyList.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập Đáp Án trước khi chấm điểm!", Toast.LENGTH_SHORT).show();
            } else {
                openGradeFragment();
            }
        });

        danhSachLop = dbHelper.layDanhSachLop();

        spinnerThongKe = v.findViewById(R.id.spinnerThongKe);
        listTenLop = new ArrayList<>();
        listTenLop.add("Thống kê");

        for (Lop lop : danhSachLop) {
            listTenLop.add(lop.getTenLop() + " (" + lop.getNienKhoa() + ")");
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listTenLop);
        spinnerThongKe.setAdapter(spinnerAdapter);

        spinnerThongKe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Lop lopDuocChon = danhSachLop.get(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
                boolean isInserted = dbHelper.themLop(tenLop, nienKhoa);
                if (isInserted) {
                    Toast.makeText(getContext(), "Thêm lớp thành công!", Toast.LENGTH_SHORT).show();
                    danhSachLop.clear();
                    danhSachLop.addAll(dbHelper.layDanhSachLop());

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
                SavedKey deletedKey = savedKeyList.get(position);
                if (kyThiId != -1) {
                    dbHelper.xoaMaDe(kyThiId, deletedKey.getMaDe());
                }
                savedKeyList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSavedKeys);
    }

    private void openAnswerKey(SavedKey item, int pos) {
        Fragment_ChiTiet_DapAn fragment = new Fragment_ChiTiet_DapAn();

        Bundle b = new Bundle();
        b.putInt("QUESTION_COUNT", questionCount);
        if (item != null) {
            b.putSerializable("EXISTING_KEY", item);
            b.putInt("EDIT_POSITION", pos);
        }
        fragment.setArguments(b);

        requireActivity().getSupportFragmentManager().beginTransaction()
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