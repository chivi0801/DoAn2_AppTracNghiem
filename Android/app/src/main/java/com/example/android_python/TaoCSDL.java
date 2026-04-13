package com.example.android_python;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaoCSDL extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "AppChamThi.db";
    private static final int DATABASE_VERSION = 2; // Tăng version để cập nhật bảng
    SQLiteOpenHelper dbHelper;
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
                "AnhBaiLam TEXT, AnhBaiLam_TenThiSinh TEXT, AnhBaiLam_Lop TEXT, TongDiem REAL, " +
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
        try {
            // 1. Xóa tất cả đáp án thuộc về kỳ thi này trước (để tránh rác)
            db.delete("BoDapAn", "KyThi_ID = ?", new String[]{String.valueOf(kyThiId)});

            // 2. Xóa chính cái Kỳ thi đó
            // db.delete trả về số dòng bị xóa. Nếu > 0 nghĩa là xóa thành công.
            int result = db.delete("KyThi", "KyThi_ID = ?", new String[]{String.valueOf(kyThiId)});

            return result > 0;
        } catch (Exception e) {
            return false;
        } finally {
            db.close();
        }
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
    public long themLop(int gvId, String tenLop, String nienKhoa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("GV_ID", gvId);
        cv.put("TenLop", tenLop);
        cv.put("NienKhoa", nienKhoa);

        return db.insert("Lop", null, cv); // Trả về ID của dòng vừa chèn hoặc -1 nếu lỗi
    }

    public boolean themKyThiLop(int kyThiId, int lopId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("KyThi_ID", kyThiId);
        cv.put("Lop_ID", lopId);
        long result = db.insert("KyThi_Lop", null, cv);
        return result != -1;
    }
    public ArrayList<Lop> layDanhSachLopTheoKyThi(int kyThiId) {
        ArrayList<Lop> danhSach = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Join bảng Lop và KyThi_Lop để lấy những lớp thuộc kỳ thi cụ thể
        String query = "SELECT L.* FROM Lop L " +
                "JOIN KyThi_Lop KL ON L.Lop_ID = KL.Lop_ID " +
                "WHERE KL.KyThi_ID = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(kyThiId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0); // Lop_ID
                String tenLop = cursor.getString(2); // TenLop (Cột 1 là GV_ID)
                String nienKhoa = cursor.getString(3); // NienKhoa

                danhSach.add(new Lop(id, tenLop, nienKhoa));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }

    // Hàm lấy toàn bộ danh sách Kỳ Thi để hiển thị lên RecyclerView
    public long themBoDapAn(String maDe, int kyThiId, String dapAn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Tên cột màu xanh lá cây phải gõ chính xác y hệt lúc CREATE TABLE
        values.put("MaDe", maDe);
        values.put("KyThi_ID", kyThiId);
        values.put("DapAn", dapAn);

        return db.insert("BoDapAn", null, values);
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
    public ArrayList<Lop> layDanhSachLopCuaGV(int gvId) {
        ArrayList<Lop> danhSach = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Lop WHERE GV_ID = ?", new String[]{String.valueOf(gvId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String tenLop = cursor.getString(2);
                String nienKhoa = cursor.getString(3);
                danhSach.add(new Lop(id, tenLop, nienKhoa));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return danhSach;
    }
    public boolean goLopKhoiKyThi(int kyThiId, int lopId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete("KyThi_Lop", "KyThi_ID=? AND Lop_ID=?",
                new String[]{String.valueOf(kyThiId), String.valueOf(lopId)});
        return result > 0;
    }
    // Nằm trong file TaoCSDL.java
    public boolean kiemTraMaDeTonTai(int kyThiId, String maDe) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM BoDapAn WHERE KyThi_ID = ? AND MaDe = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(kyThiId), maDe});

        boolean exists = (cursor.getCount() > 0);
        cursor.close();

        return exists;
    }
    public ArrayList<Lop> layDanhSachLopDuyNhat(int gvId) {
        ArrayList<Lop> listLop = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();



        String query = "SELECT Lop_ID, TenLop, NienKhoa FROM Lop WHERE GV_ID = ?";

        android.database.Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gvId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String tenLop = cursor.getString(1);
                String nienKhoa = cursor.getString(2);

                // Nạp vào Model Lop của ông
                listLop.add(new Lop(id, tenLop, nienKhoa));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listLop;
    }
    public long themThiSinhVaoDB(String thiSinhId, int lopId, String hoTen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("ThiSinh_ID", thiSinhId);
        values.put("Lop_ID", lopId);
        values.put("HoTen", hoTen);

        return db.insert("ThiSinh", null, values);
    }

    public boolean kiemTraThiSinhTonTai(String thiSinhId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ThiSinh WHERE ThiSinh_ID = ?", new String[]{thiSinhId});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    // Nhớ thêm hàm này vào TaoCSDL.java nhé
    public ArrayList<ThiSinh> layDanhSachThiSinhTheoLop(int lopId) {
        ArrayList<ThiSinh> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ThiSinh WHERE Lop_ID = ?", new String[]{String.valueOf(lopId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new ThiSinh(cursor.getString(0), cursor.getInt(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public long luuBaiThi(int kyThiId, String maDe, String thiSinhId, String pathAnhChinh, String pathAnhTen, String pathAnhLop, double tongDiem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("KyThi_ID", kyThiId);
        values.put("MaDe", maDe);
        values.put("ThiSinh_ID", thiSinhId);
        values.put("AnhBaiLam", pathAnhChinh);
        values.put("AnhBaiLam_TenThiSinh", pathAnhTen);
        values.put("AnhBaiLam_Lop", pathAnhLop);
        values.put("TongDiem", tongDiem);
        return db.insert("BaiThi", null, values);
    }

    public void luuChiTietBaiThi(long baiThiId, int cauSo, String dapAnThiSinh, String trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("BaiThi_ID", baiThiId);
        values.put("CauSo", cauSo);
        values.put("DapAnThiSinh", dapAnThiSinh);
        values.put("TrangThai", trangThai);
        db.insert("ChiTietBaiThi", null, values);
    }
    public boolean capNhatThiSinh(String idCu, String idMoi, String tenMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("ThiSinh_ID", idMoi);
        values.put("HoTen", tenMoi);

        // Cập nhật dữ liệu tại dòng có ThiSinh_ID bằng với idCu
        int rowsAffected = db.update("ThiSinh", values, "ThiSinh_ID = ?", new String[]{idCu});

        // Nếu rowsAffected > 0 nghĩa là đã cập nhật thành công ít nhất 1 dòng
        return rowsAffected > 0;
    }
}