package com.example.android_python; // Thay bằng package của bạn

public class ThiSinh {
    private String thiSinhId;
    private int lopId;
    private String hoTen;

    // Constructor đầy đủ tham số
    public ThiSinh(String thiSinhId, int lopId, String hoTen) {
        this.thiSinhId = thiSinhId;
        this.lopId = lopId;
        this.hoTen = hoTen;
    }

    // Constructor rỗng (Cần thiết nếu dùng Firebase sau này hoặc deserialize)
    public ThiSinh() {
    }

    // Các hàm Getter và Setter
    public String getThiSinhId() {
        return thiSinhId;
    }

    public void setThiSinhId(String thiSinhId) {
        this.thiSinhId = thiSinhId;
    }

    public int getLopId() {
        return lopId;
    }

    public void setLopId(int lopId) {
        this.lopId = lopId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
}