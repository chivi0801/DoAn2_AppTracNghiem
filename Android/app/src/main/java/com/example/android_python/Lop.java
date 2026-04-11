package com.example.android_python;
public class Lop {
    private int lopId;
    private String tenLop;
    private String nienKhoa;

    public Lop(int lopId, String tenLop, String nienKhoa) {
        this.lopId = lopId;
        this.tenLop = tenLop;
        this.nienKhoa = nienKhoa;
    }

    public int getLopId() { return lopId; }
    public String getTenLop() { return tenLop; }
    public String getNienKhoa() { return nienKhoa; }
}