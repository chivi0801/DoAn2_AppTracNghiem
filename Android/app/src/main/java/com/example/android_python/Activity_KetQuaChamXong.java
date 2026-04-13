package com.example.android_python;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Activity_KetQuaChamXong extends AppCompatActivity {

    private ImageView kq_vuaChamXong;
    private Button btn_ChamLai, btn_LuuBai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_chamxong);

        // Khởi tạo views
        kq_vuaChamXong = findViewById(R.id.kq_vuaChamXong);
        btn_ChamLai = findViewById(R.id.btn_ChamLai);
        btn_LuuBai = findViewById(R.id.btn_LuuBai);

        // Bắt các Intent từ Camera
        String imagePath = getIntent().getStringExtra("PATH_ANH_CHINH");
        String tenPath = getIntent().getStringExtra("PATH_ANH_TEN");
        String lopPath = getIntent().getStringExtra("PATH_ANH_LOP");
        String thiSinhID = getIntent().getStringExtra("SBD");
        String maDe = getIntent().getStringExtra("MADE");
        double tongDiem = getIntent().getDoubleExtra("DIEM", 0.0);
        String jsonDapAn = getIntent().getStringExtra("JSON_DAPAN");

        String lopID = getIntent().getStringExtra("LOP_ID");
        String kyThiID = getIntent().getStringExtra("KYTHI_ID");


        // Hiển thị ảnh kết quả
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                kq_vuaChamXong.setImageBitmap(myBitmap);
            } else {
                Toast.makeText(this, "Không tìm thấy ảnh kết quả", Toast.LENGTH_SHORT).show();
            }
        }

        // Xử lý nút Chấm lại: quay về màn hình trước (Camera)
        btn_ChamLai.setOnClickListener(v -> {
            finish();
        });

        // Xử lý nút Lưu bài: thực hiện lưu và thông báo
        btn_LuuBai.setOnClickListener(v -> {
            try {
                // 1. Di chuyển ảnh từ Cache sang bộ nhớ chính thức
                String permanentChinhPath = saveFilePermanently(imagePath, "CHINH");
                String permanentTenPath = saveFilePermanently(tenPath, "TEN");
                String permanentLopPath = saveFilePermanently(lopPath, "LOP");

                // 2. Lưu vào CSDL
                TaoCSDL db = new TaoCSDL(this);
                
                // Ép kiểu ID về int, mặc định là -1 nếu không có
                int kId = -1;
                int lId = -1;
                try {
                    if (kyThiID != null) {
                        kId = Integer.parseInt(kyThiID);
                    }
                    if (lopID != null) {
                        lId = Integer.parseInt(lopID);
                    }
                } catch (NumberFormatException e) {
                    Log.e("SAVE_ERROR", "Lỗi định dạng ID: " + kyThiID + ", " + lopID);
                }
                
                if (kId == -1 || lId == -1) {
                    Toast.makeText(this, "Lỗi: Không xác định được Kỳ thi hoặc Lớp ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kiểm tra và tự động thêm thí sinh nếu chưa có trong DB
                if (thiSinhID != null && !thiSinhID.isEmpty()) {
                    if (!db.kiemTraThiSinhTonTai(thiSinhID)) {
                        db.themThiSinhVaoDB(thiSinhID, lId, "(chưa cập nhật)"); // Họ tên để trống
                        Log.d("SAVE_INFO", "Đã tự động thêm thí sinh mới: " + thiSinhID);
                    }
                }
                
                long baiThiId = db.luuBaiThi(kId, maDe, thiSinhID, permanentChinhPath, permanentTenPath, permanentLopPath, tongDiem);

                if (baiThiId != -1) {
                    // 3. Lưu chi tiết từng câu từ JSON
                    if (jsonDapAn != null && !jsonDapAn.isEmpty()) {
                        try {
                            JSONArray array = new JSONArray(jsonDapAn);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                // Kiểm tra các key từ JSON trả về (phải khớp với Python)
                                int cauSo = obj.optInt("cauSo", i + 1);
                                String dapAn = obj.optString("dapAnThiSinh", "");
                                String trangThai = obj.optString("trangThai", "");

                                db.luuChiTietBaiThi(baiThiId, cauSo, dapAn, trangThai);
                            }
                        } catch (Exception jsonEx) {
                            Log.e("JSON_ERROR", "Lỗi phân tích JSON đáp án: " + jsonEx.getMessage());
                        }
                    }

                    Toast.makeText(this, "Đã lưu kết quả thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e("DB_ERROR", "Lỗi db.insert trả về -1. Kiểm tra khóa ngoại SBD hoặc MaDe.");
                    Toast.makeText(this, "Lỗi: Không thể lưu vào CSDL (Có thể do sai SBD hoặc Mã đề)", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.e("SAVE_ERROR", "Lỗi lưu bài thi: " + e.getMessage());
                Toast.makeText(this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // hàm lưu file tạm vào bộ nhớ
    private String saveFilePermanently(String tempPath, String type) throws IOException {
        if (tempPath == null) return null;
        File sourceFile = new File(tempPath);
        if (!sourceFile.exists()) return null;

        File storageDir = new File(getExternalFilesDir(null), "KetQua_ChinhThuc");
        if (!storageDir.exists()) storageDir.mkdirs();

        File destFile = new File(storageDir, sourceFile.getName().replace("TEMP_", ""));
        
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
        
        // Xóa file tạm sau khi lưu xong
        sourceFile.delete();
        
        return destFile.getAbsolutePath();
    }
}
