package com.example.android_python;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "AppChamThi.db";
    private static final int DATABASE_VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng GiangVien
        // 1. Bảng GiangVien (Đã sửa Email thành TenTaiKhoan)
        db.execSQL("CREATE TABLE GiangVien (" +
                "GV_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TenTaiKhoan TEXT UNIQUE, " + // Dùng Tên tài khoản làm duy nhất
                "HoTenGV TEXT, " +
                "MatKhau TEXT)");

        // 2. Bảng Lop
        db.execSQL("CREATE TABLE Lop (" +
                "Lop_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TenLop TEXT, NienKhoa TEXT)");

        // 3. Bảng ThiSinh (Dùng TEXT cho ThiSinh_ID vì mã HS thường có cả chữ và số)
        db.execSQL("CREATE TABLE ThiSinh (" +
                "ThiSinh_ID TEXT PRIMARY KEY, " +
                "Lop_ID INTEGER, HoTen TEXT, " +
                "FOREIGN KEY(Lop_ID) REFERENCES Lop(Lop_ID))");

        // 4. Bảng KyThi
        db.execSQL("CREATE TABLE KyThi (" +
                "KyThi_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "GV_ID INTEGER, TenKyThi TEXT, LoaiPhieu TEXT, SoCau INTEGER, " +
                "FOREIGN KEY(GV_ID) REFERENCES GiangVien(GV_ID))");

        // 5. Bảng KyThi_Lop (Quan hệ n-n)
        db.execSQL("CREATE TABLE KyThi_Lop (" +
                "KyThi_ID INTEGER, Lop_ID INTEGER, " +
                "PRIMARY KEY(KyThi_ID, Lop_ID), " +
                "FOREIGN KEY(KyThi_ID) REFERENCES KyThi(KyThi_ID), " +
                "FOREIGN KEY(Lop_ID) REFERENCES Lop(Lop_ID))");

        // 6. Bảng BoDapAn (Đã sửa khóa chính thành MaDe)
        db.execSQL("CREATE TABLE BoDapAn (" +
                "MaDe TEXT, KyThi_ID INTEGER, DapAn TEXT, " +
                "PRIMARY KEY(MaDe, KyThi_ID), " + // Đảm bảo 1 mã đề không trùng trong cùng 1 kỳ thi
                "FOREIGN KEY(KyThi_ID) REFERENCES KyThi(KyThi_ID))");

        // 7. Bảng BaiThi (Đã sửa lại các khóa ngoại cho khớp)
        db.execSQL("CREATE TABLE BaiThi (" +
                "BaiThi_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "KyThi_ID INTEGER, Lop_ID INTEGER, " +
                "MaDe TEXT, ThiSinh_ID TEXT, " +
                "AnhBaiLam TEXT, AnhBaiLam_TenThiSinh TEXT, TongDiem REAL, " +
                "FOREIGN KEY(KyThi_ID) REFERENCES KyThi(KyThi_ID), " +
                "FOREIGN KEY(Lop_ID) REFERENCES Lop(Lop_ID), " +
                "FOREIGN KEY(MaDe, KyThi_ID) REFERENCES BoDapAn(MaDe, KyThi_ID), " +
                "FOREIGN KEY(ThiSinh_ID) REFERENCES ThiSinh(ThiSinh_ID))");

        // 8. Bảng ChiTietBaiThi
        db.execSQL("CREATE TABLE ChiTietBaiThi (" +
                "BaiThi_ID INTEGER, CauSo INTEGER, " +
                "DapAnThiSinh TEXT, TrangThai TEXT, " +
                "PRIMARY KEY(BaiThi_ID, CauSo), " +
                "FOREIGN KEY(BaiThi_ID) REFERENCES BaiThi(BaiThi_ID))");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ChiTietBaiThi");
        db.execSQL("DROP TABLE IF EXISTS BaiThi");
        db.execSQL("DROP TABLE IF EXISTS BoDapAn");
        db.execSQL("DROP TABLE IF EXISTS KyThi_Lop");
        db.execSQL("DROP TABLE IF EXISTS KyThi");
        db.execSQL("DROP TABLE IF EXISTS ThiSinh");
        db.execSQL("DROP TABLE IF EXISTS Lop");
        db.execSQL("DROP TABLE IF EXISTS GiangVien");
        onCreate(db);
    }
    public boolean themGiangVien(String tenTaiKhoan, String hoTen, String matKhau) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("TenTaiKhoan", tenTaiKhoan);
        cv.put("HoTenGV", hoTen);
        cv.put("MatKhau", matKhau);

        long result = db.insert("GiangVien", null, cv);
        return result != -1;
    }
    public int KiemTraDangNhap(String tenTaiKhoan, String matKhau) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Dùng con trỏ (Cursor) để tìm trong bảng GiangVien
        Cursor cursor = db.rawQuery("SELECT GV_ID FROM GiangVien WHERE TenTaiKhoan=? AND MatKhau=?", new String[]{tenTaiKhoan, matKhau});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int gv_id = cursor.getInt(0);
            cursor.close();
            return gv_id;
        } else {
            cursor.close();
            return -1;
        }
    }
    public boolean themKyThi(int gvId, String tenKyThi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("GV_ID", gvId); // Khóa ngoại liên kết với giáo viên
        cv.put("TenKyThi", tenKyThi);
        // Nếu bảng KyThi của bạn có thêm cột nào (như NgayTao, TrangThai...), bạn có thể thêm cv.put() ở đây

        long result = db.insert("KyThi", null, cv);
        return result != -1;
    }

    // 2. Hàm lấy danh sách TÊN các kỳ thi của một giáo viên cụ thể
    public ArrayList<String> layDanhSachKyThi(int gvId) {
        ArrayList<String> dsKyThi = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Chỉ lấy những kỳ thi có GV_ID khớp với giáo viên đang đăng nhập
        Cursor cursor = db.rawQuery("SELECT TenKyThi FROM KyThi WHERE GV_ID = ?", new String[]{String.valueOf(gvId)});

        if (cursor.moveToFirst()) {
            do {
                // Cột TenKyThi nằm ở vị trí số 0 trong kết quả SELECT
                dsKyThi.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dsKyThi;
    }
}