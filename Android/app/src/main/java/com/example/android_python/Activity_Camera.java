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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Activity_Camera extends AppCompatActivity {

    private Map<String, String> boDapAn = new HashMap<>();
    
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnChup;
    private volatile boolean isProcessing = false;
    private volatile boolean stopScanning = false;

    private int currentLopId;
    private int currentKyThiId;
    private TaoCSDL dbHelper;

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private static final int REQUEST_CODE_PERMISSIONS = 1001;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Bắt Intent từ ChiTietKyThi
        currentLopId = getIntent().getIntExtra("LOP_ID", -1);
        currentKyThiId = getIntent().getIntExtra("KYTHI_ID", -1);

        dbHelper = new TaoCSDL(this);
        loadBoDapAn();

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

    private void loadBoDapAn() {
        List<SavedKey> list = dbHelper.layDanhSachMaDe(currentKyThiId);
        if (list != null) {
            for (SavedKey sk : list) {
                boDapAn.put(sk.getMaDe(), sk.getDapAn());
            }
        }
        Log.d("BO_DAP_AN", "Đã tải " + boDapAn.size() + " mã đề cho kỳ thi " + currentKyThiId);
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
        if (imageCapture == null || isProcessing || stopScanning) return;
        isProcessing = true;

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
                isProcessing = false;
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
                PyObject pyModule = py.getModule("xuLyAnh");

                // 1. Gọi hàm và nhận về 1 list các PyObject
                PyObject result = pyModule.callAttr("XuLyAnh", inputFile.getAbsolutePath(), boDapAn);
                List<PyObject> results = result.asList();

                // 2. Lấy từng thành phần theo đúng thứ tự return trong Python
                PyObject anhWarped = results.get(0);    // Ảnh bài chấm xong
                PyObject tenROI = results.get(1);      // ROI Tên
                PyObject lopROI = results.get(2);      // ROI Lớp
                String thiSinhID = results.get(3).toString();
                String maDe = results.get(4).toString();
                double tongDiem = results.get(5).toDouble();
                String jsonDapAn = results.get(6).toString();

                // 3. Lưu các ảnh vào bộ nhớ tạm (Cache) để hiển thị, chưa lưu chính thức
                File storageDir = new File(getCacheDir(), "Temp_KetQua");
                if (!storageDir.exists()) storageDir.mkdirs();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());

                File fileAnhChinh = new File(storageDir, "TEMP_CHINH_" + timeStamp + ".jpg");
                File fileAnhTen = new File(storageDir, "TEMP_TEN_" + timeStamp + ".jpg");
                File fileAnhLop = new File(storageDir, "TEMP_LOP_" + timeStamp + ".jpg");

                // Sử dụng Chaquopy để lưu ảnh vào cache
                PyObject cv2 = py.getModule("cv2");
                cv2.callAttr("imwrite", fileAnhChinh.getAbsolutePath(), anhWarped);
                cv2.callAttr("imwrite", fileAnhTen.getAbsolutePath(), tenROI);
                cv2.callAttr("imwrite", fileAnhLop.getAbsolutePath(), lopROI);

                // 4. Dừng quét và chuyển dữ liệu sang màn hình kết quả hoặc lưu DB
                stopScanning = true;

                runOnUiThread(() -> {
                    // Chuyển sang Activity_KetQuaChamXong kèm theo tất cả thông tin
                    Intent intent = new Intent(Activity_Camera.this, Activity_KetQuaChamXong.class);
                    intent.putExtra("PATH_ANH_CHINH", fileAnhChinh.getAbsolutePath());
                    intent.putExtra("PATH_ANH_TEN", fileAnhTen.getAbsolutePath());
                    intent.putExtra("PATH_ANH_LOP", fileAnhLop.getAbsolutePath());
                    intent.putExtra("SBD", thiSinhID);
                    intent.putExtra("MADE", maDe);
                    intent.putExtra("DIEM", tongDiem);
                    intent.putExtra("JSON_DAPAN", jsonDapAn);

                    // Truyền thêm Lop_ID và KyThi_ID mà bạn đang chấm
                    intent.putExtra("LOP_ID", String.valueOf(currentLopId));
                    intent.putExtra("KYTHI_ID", String.valueOf(currentKyThiId));

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
                    if (isProcessing || stopScanning || imageCapture == null) {
                        image.close();
                        return;
                    }

                    isProcessing = true;
                    image.close();

                    File cacheFile = new File(getCacheDir(), "temp_frame.jpg");
                    ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(cacheFile).build();

                    imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                            processImageWithPython(cacheFile);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException e) {
                            isProcessing = false;
                            Log.e("ANALYSIS_ERROR", "Error capturing high quality image", e);
                        }
                    });
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