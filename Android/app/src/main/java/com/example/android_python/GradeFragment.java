package com.example.android_python;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class GradeFragment extends Fragment {

    private PreviewView viewFinder;
    private View scanLine, scannerFrame;
    private EditText edtGradeResult;
    private Button btnAcceptGrade;
    private String examName;

    // Bộ xử lý xin quyền Camera hiện đại
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Ứng dụng cần quyền Camera để chấm điểm!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            examName = getArguments().getString("EXAM_NAME", "Môn học");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grade, container, false);

        // Ánh xạ các View từ XML của bạn
        viewFinder = v.findViewById(R.id.viewFinder);
        scanLine = v.findViewById(R.id.scanLine);
        scannerFrame = v.findViewById(R.id.scannerFrame);
        edtGradeResult = v.findViewById(R.id.edtGradeResult);
        btnAcceptGrade = v.findViewById(R.id.btnAcceptGrade);

        setupToolbar(v);
        checkPermissionAndStartCamera();
        runScanAnimation();

        // Xử lý nút Nhận
        btnAcceptGrade.setOnClickListener(view -> {
            String result = edtGradeResult.getText().toString();
            Toast.makeText(getContext(), "Đã nhận kết quả: " + result, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });

        return v;
    }

    private void checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Cấu hình Preview (Luồng hiển thị lên màn hình)
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Chọn Camera sau
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Liên kết Camera với Lifecycle của Fragment
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(getContext(), "Lỗi khởi tạo Camera!", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void runScanAnimation() {
        // Chờ scannerFrame vẽ xong để lấy chiều cao chính xác
        scannerFrame.post(() -> {
            float startY = 0f;
            float endY = scannerFrame.getHeight() - scanLine.getHeight();

            ObjectAnimator animator = ObjectAnimator.ofFloat(scanLine, "translationY", startY, endY);
            animator.setDuration(2500); // 2.5 giây cho 1 vòng quét
            animator.setRepeatCount(ValueAnimator.INFINITE); // Chạy vô hạn
            animator.setRepeatMode(ValueAnimator.REVERSE); // Chạy xuống rồi chạy ngược lên
            animator.start();
        });
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
        if (title != null) title.setText("Chấm Điểm: " + examName);
    }
}