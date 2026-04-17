package com.example.android_python;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
            TaoCSDL db = new TaoCSDL(this);
            int tempLopId = -1;
            try {
                if (lopID != null) tempLopId = Integer.parseInt(lopID);
            } catch (Exception e) {}
            final int currentLopId = tempLopId;

            int lopIdCuaSbd = db.layLopIDCuaThiSinh(thiSinhID);

            if (lopIdCuaSbd != -1 && lopIdCuaSbd != currentLopId) {
                String tenLopCu = db.layTenLop(lopIdCuaSbd);
                new AlertDialog.Builder(this)
                        .setTitle("Lưu ý!")
                        .setMessage("SBD " + thiSinhID + " đã tồn tại ở lớp [" + tenLopCu + "]. Bạn có muốn chuyển thí sinh này sang lớp hiện tại và lưu điểm không?")
                        .setPositiveButton("Chuyển và Lưu", (dialog, which) -> {
                            db.capNhatLopChoThiSinh(thiSinhID, currentLopId);
                            thucHienLuuBaiThi(db, imagePath, tenPath, lopPath, kyThiID, lopID, thiSinhID, maDe, tongDiem, jsonDapAn);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                thucHienLuuBaiThi(db, imagePath, tenPath, lopPath, kyThiID, lopID, thiSinhID, maDe, tongDiem, jsonDapAn);
            }
        });
    }

    private void thucHienLuuBaiThi(TaoCSDL db, String imagePath, String tenPath, String lopPath, String kyThiID, String lopID, String thiSinhID, String maDe, double tongDiem, String jsonDapAn) {
        try {
            // 1. Di chuyển ảnh từ Cache sang bộ nhớ chính thức
            String permanentChinhPath = saveFilePermanently(imagePath, "CHINH");
            String permanentTenPath = saveFilePermanently(tenPath, "TEN");
            String permanentLopPath = saveFilePermanently(lopPath, "LOP");

            // Ép kiểu ID về int
            int kId = -1;
            int lId = -1;
            try {
                if (kyThiID != null) kId = Integer.parseInt(kyThiID);
                if (lopID != null) lId = Integer.parseInt(lopID);
            } catch (NumberFormatException e) {
                Log.e("SAVE_ERROR", "Lỗi định dạng ID");
            }

            if (kId == -1 || lId == -1) {
                Toast.makeText(this, "Lỗi: Không xác định được Kỳ thi hoặc Lớp ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra và tự động thêm thí sinh nếu chưa có trong DB (Trường hợp SBD mới hoàn toàn)
            if (thiSinhID != null && !thiSinhID.isEmpty()) {
                if (!db.kiemTraThiSinhTonTai(thiSinhID)) {
                    db.themThiSinhVaoDB(thiSinhID, lId, "(chưa cập nhật)");
                }
            }

            long baiThiId = db.luuBaiThi(kId, maDe, thiSinhID, permanentChinhPath, permanentTenPath, permanentLopPath, tongDiem);

            if (baiThiId != -1) {
                if (jsonDapAn != null && !jsonDapAn.isEmpty()) {
                    JSONArray array = new JSONArray(jsonDapAn);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int cauSo = obj.optInt("cauSo", i + 1);
                        String dapAn = obj.optString("dapAnThiSinh", "");
                        String trangThai = obj.optString("trangThai", "");
                        db.luuChiTietBaiThi(baiThiId, cauSo, dapAn, trangThai);
                    }
                }
                Toast.makeText(this, "Đã lưu kết quả thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi: Không thể lưu vào CSDL", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("SAVE_ERROR", "Lỗi: " + e.getMessage());
        }
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
