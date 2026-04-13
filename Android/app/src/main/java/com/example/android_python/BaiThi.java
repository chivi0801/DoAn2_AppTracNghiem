package com.example.android_python;

public class BaiThi {
    private int baiThiId;
    private int kyThiId;
    private String maDe;
    private String thiSinhId;
    private String anhBaiLam;
    private String anhBaiLamTenThiSinh;
    private String anhBaiLamLop;
    private double tongDiem;

    public BaiThi(int baiThiId, int kyThiId, String maDe, String thiSinhId, String anhBaiLam, String anhBaiLamTenThiSinh, String anhBaiLamLop, double tongDiem) {
        this.baiThiId = baiThiId;
        this.kyThiId = kyThiId;
        this.maDe = maDe;
        this.thiSinhId = thiSinhId;
        this.anhBaiLam = anhBaiLam;
        this.anhBaiLamTenThiSinh = anhBaiLamTenThiSinh;
        this.anhBaiLamLop = anhBaiLamLop;
        this.tongDiem = tongDiem;
    }

    public int getBaiThiId() { return baiThiId; }
    public int getKyThiId() { return kyThiId; }
    public String getMaDe() { return maDe; }
    public String getThiSinhId() { return thiSinhId; }
    public String getAnhBaiLam() { return anhBaiLam; }
    public String getAnhBaiLamTenThiSinh() { return anhBaiLamTenThiSinh; }
    public String getAnhBaiLamLop() { return anhBaiLamLop; }
    public double getTongDiem() { return tongDiem; }
}
