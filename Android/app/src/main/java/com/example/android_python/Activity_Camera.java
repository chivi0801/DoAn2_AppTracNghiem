package com.example.android_python;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Activity_Camera extends AppCompatActivity {

    private Map<String, String> boDapAn = new HashMap<>();
    
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnChup;
    private boolean isProcessing = false;
    private boolean stopScanning = false;

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private static final int REQUEST_CODE_PERMISSIONS = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Khởi tạo bộ đáp án
        boDapAn.put("001", "ABCBACBCABCABDBCABDBCABACBDADCADCABDABCA");
        boDapAn.put("002", "BCABCABDBCABDBCABACBDADCADCABDABCACCCABC");
        boDapAn.put("003", "CABDABCAABCBACBCABCABDBCABDBCABACBDADCAD");
        boDapAn.put("004", "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");

        previewView = findViewById(R.id.previewView);
        btnChup = findViewById(R.id.btn_chupVaCham);

        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);

        if (checkPermissions()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        btnChup.setOnClickListener(v -> takePhoto()); // nút chụp
    }
    //--------------------------------------------------------------------------------------------------

    //
    @Override
    protected void onResume() {
        super.onResume();
        stopScanning = false;
        isProcessing = false;
    }

    private void takePhoto() { // dành cho nút chụp tay
        if (imageCapture == null) return;

        File storageDir = new File(getExternalFilesDir(null), "LuuAnh");
        if (!storageDir.exists()) storageDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        File photoFile = new File(storageDir, "IMG_" + timeStamp + ".jpg");

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                runOnUiThread(() -> {
                    Toast.makeText(Activity_Camera.this, "Đang xử lý ảnh bằng Python...", Toast.LENGTH_SHORT).show();
                    // Gọi hàm xử lý ảnh cục bộ bằng Chaquopy
                    processImageWithPython(photoFile);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                runOnUiThread(() -> Toast.makeText(Activity_Camera.this, "Lỗi chụp ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    //hàm gọi xử lý ảnh bằng Chaquopy
    private void processImageWithPython(File inputFile) {
        if (stopScanning) return;
        isProcessing = true;
        
        new Thread(() -> {
            try {
                Python py = Python.getInstance();
                // Lấy module xuLyAnh.py
                PyObject pyModule = py.getModule("xuLyAnh");

                // Gọi hàm XuLyAnh(img_path) từ Python--------------------------
                // trả về một mảng numpy (ảnh đã xử lý)
                PyObject processedImageArray = pyModule.callAttr("XuLyAnh", inputFile.getAbsolutePath(), boDapAn);

                //--------------------------------------------------------------

                // Nếu chạy đến đây mà không quăng Exception nghĩa là đã tìm thấy bài và xử lý thành công
                stopScanning = true;

                // Lưu ảnh đã xử lý xuống file để ResultActivity có thể đọc
                File processedFile = new File(getExternalFilesDir(null), "processed_result.jpg");

                // Sử dụng cv2 của Python để lưu kết quả
                PyObject cv2 = py.getModule("cv2");
                cv2.callAttr("imwrite", processedFile.getAbsolutePath(), processedImageArray);

                runOnUiThread(() -> {
                    // Chuyển sang ResultActivity
                    Intent intent = new Intent(Activity_Camera.this, ResultActivity.class);
                    intent.putExtra("PROCESSED_IMAGE_PATH", processedFile.getAbsolutePath());
                    startActivity(intent);
                });

            } catch (PyException e) {
                // Lỗi Python (thường là do không tìm thấy đủ mốc định vị trong frame này)
                Log.d("SCAN_INFO", "Chưa tìm thấy bài: " + e.getMessage());
            } catch (Exception e) {
                Log.e("SYSTEM_ERROR", "Lỗi: " + e.getMessage());
            } finally {
                isProcessing = false;
                // Xóa file tạm sau khi xử lý xong frame này
                if (inputFile.exists()) {
                    inputFile.delete();
                }
            }
        }).start();
    }

    //hàm khởi tạo camera
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                        .setAspectRatioStrategy(new AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                        .build();

                Preview preview = new Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                // Cấu hình ImageAnalysis để quét liên tục
                androidx.camera.core.ImageAnalysis imageAnalysis = new androidx.camera.core.ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
                    if (isProcessing || stopScanning) {
                        image.close();
                        return;
                    }

                    // Chuyển frame thành file tạm để Python xử lý
                    try {
                        android.graphics.Bitmap bitmap = previewView.getBitmap();
                        if (bitmap != null) {
                            File cacheFile = new File(getCacheDir(), "temp_frame.jpg");
                            java.io.FileOutputStream out = new java.io.FileOutputStream(cacheFile);
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            processImageWithPython(cacheFile);
                        }
                    } catch (Exception e) {
                        Log.e("ANALYSIS_ERROR", "Error saving frame", e);
                    } finally {
                        image.close();
                    }
                });

                ViewPort viewPort = new ViewPort.Builder(new Rational(3, 4), getWindowManager().getDefaultDisplay().getRotation()).build();

                UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageCapture)
                        .addUseCase(imageAnalysis)
                        .setViewPort(viewPort)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);

            } catch (Exception e) {
                Log.e("CAMERA_X", "Camera binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // hàm kiểm tra quyền truy cập camera
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    //hàm xử lý kết quả trả về khi yêu cầu cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && checkPermissions()) {
            startCamera();
        }
    }
}