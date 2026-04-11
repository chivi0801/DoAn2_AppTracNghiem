package com.example.android_python;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;

public class TaoCSDL extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "AppChamThi.db";
    private static final int DATABASE_VERSION = 1;
    public TaoCSDL(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng GiangVien
        // 1. Bảng GiangVien (Đã sửa Email thành TenTaiKhoan)
        db.execSQL("CREATE TABLE GiangVien (" +
                "GV_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TenTaiKhoan TEXT UNIQUE, " +
                "HoTenGV TEXT, " +
                "MatKhau TEXT)");

        // 2. Bảng Lop
                db.execSQL("CREATE TABLE Lop (" +
                "Lop_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "GV_ID INTEGER, " +
                "TenLop TEXT, " +
                "NienKhoa TEXT, " +
                "FOREIGN KEY(GV_ID) REFERENCES GiangVien(GV_ID))");

        // 3. Bảng ThiSinh (Dùng TEXT cho ThiSinh_ID vì mã HS thường có cả chữ và số)
        db.execSQL("CREATE TABLE ThiSinh (" +
                "ThiSinh_ID TEXT PRIMARY KEY, " +
                "Lop_ID INTEGER, HoTen TEXT, " +
                "FOREIGN KEY(Lop_ID) REFERENCES Lop(Lop_ID))");

        // 4. Bảng KyThi
        db.execSQL("CREATE TABLE KyThi (" +
                "KyThi_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "GV_ID INTEGER, TenKyThi TEXT, LoaiPhieu TEXT, " +
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
                "KyThi_ID INTEGER, " +
                "MaDe TEXT, ThiSinh_ID TEXT, " +
                "AnhBaiLam TEXT, AnhBaiLam_TenThiSinh TEXT, TongDiem REAL, " +
                "FOREIGN KEY(KyThi_ID) REFERENCES KyThi(KyThi_ID), " +
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

        long result = db.insert("KyThi", null, cv);
        return result != -1;
    }

    // 2. Hàm lấy danh sách TÊN các kỳ thi của một giáo viên cụ thể
    // 1. Hàm lấy danh sách đầy đủ THEO GIẢNG VIÊN
    public ArrayList<Exam> layDanhSachKyThiTheoGV(int gvId) {
        ArrayList<Exam> danhSach = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Lọc theo GV_ID để không bị lẫn lộn dữ liệu giữa các tài khoản
        Cursor cursor = db.rawQuery("SELECT * FROM KyThi WHERE GV_ID = ?", new String[]{String.valueOf(gvId)});

        if (cursor.moveToFirst()) {
            do {
                int kyThiId = cursor.getInt(0);         // KyThi_ID
                String tenKyThi = cursor.getString(2);  // TenKyThi
                String loaiPhieu = cursor.getString(3); // LoaiPhieu

                String date = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        .format(java.util.Calendar.getInstance().getTime());

                // Đưa ID vào đối tượng Exam (Đã bỏ soCau)
                danhSach.add(new Exam(kyThiId, tenKyThi, date, loaiPhieu));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // 2. Hàm xóa kỳ thi khỏi CSDL
    public boolean xoaKyThi(int kyThiId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa luôn các dữ liệu liên quan để tránh rác database (Mã đề, bài thi...)
        db.delete("BoDapAn", "KyThi_ID=?", new String[]{String.valueOf(kyThiId)});
        long result = db.delete("KyThi", "KyThi_ID=?", new String[]{String.valueOf(kyThiId)});
        return result > 0;
    }
    public boolean ThemKyThi(int gvId, String tenKyThi, String loaiPhieu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("GV_ID", gvId);
        cv.put("TenKyThi", tenKyThi);
        cv.put("LoaiPhieu", loaiPhieu);

        long result = db.insert("KyThi", null, cv);
        return result != -1;
    }
    public boolean themLop(int gvId, String tenLop, String nienKhoa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("GV_ID", gvId);
        cv.put("TenLop", tenLop);
        cv.put("NienKhoa", nienKhoa);

        long result = db.insert("Lop", null, cv);
        return result != -1; // Trả về true nếu thêm thành công
    }
    public ArrayList<Lop> layDanhSachLop(int gvId) {
        ArrayList<Lop> danhSach = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM Lop WHERE GV_ID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gvId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String tenLop = cursor.getString(2); // Vị trí 2 vì 0:Lop_ID, 1:GV_ID, 2:TenLop, 3:NienKhoa
                String nienKhoa = cursor.getString(3);

                // Thêm vào danh sách
                danhSach.add(new Lop(id, tenLop, nienKhoa));
            } while (cursor.moveToNext());
        }

        // Đóng con trỏ để giải phóng bộ nhớ
        cursor.close();

        return danhSach;
    }
    public boolean xoaLop(int lopId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa dòng có Lop_ID tương ứng
        long result = db.delete("Lop", "Lop_ID=?", new String[]{String.valueOf(lopId)});
        return result > 0; // Trả về true nếu xóa thành công
    }

    // Hàm lấy toàn bộ danh sách Kỳ Thi để hiển thị lên RecyclerView
    public boolean themMaDe(int kyThiId, String maDe, String dapAn) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("KyThi_ID", kyThiId);
        cv.put("MaDe", maDe);
        cv.put("DapAn", dapAn);
        long result = db.insert("BoDapAn", null, cv);
        return result != -1;
    }

    public boolean suaMaDe(int kyThiId, String maDeCu, String maDeMoi, String dapAnMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("MaDe", maDeMoi);
        cv.put("DapAn", dapAnMoi);
        // Sửa theo KyThi_ID và MaDe cũ
        int result = db.update("BoDapAn", cv, "KyThi_ID=? AND MaDe=?",
                new String[]{String.valueOf(kyThiId), maDeCu});
        return result > 0;
    }

    public boolean xoaMaDe(int kyThiId, String maDe) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("BoDapAn", "KyThi_ID=? AND MaDe=?",
                new String[]{String.valueOf(kyThiId), maDe});
        return result > 0;
    }

    public java.util.List<SavedKey> layDanhSachMaDe(int kyThiId) {
        java.util.List<SavedKey> list = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM BoDapAn WHERE KyThi_ID=?",
                new String[]{String.valueOf(kyThiId)});

        if (cursor.moveToFirst()) {
            do {
                String maDe = cursor.getString(cursor.getColumnIndexOrThrow("MaDe"));
                String dapAn = cursor.getString(cursor.getColumnIndexOrThrow("DapAn"));
                list.add(new SavedKey(maDe, dapAn));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}