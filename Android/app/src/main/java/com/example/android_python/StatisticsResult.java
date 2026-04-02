package com.example.android_python;

public class StatisticsResult {
    private String maTS, maDe, cauDung, diem;

    public StatisticsResult(String maTS, String maDe, String cauDung, String diem) {
        this.maTS = maTS;
        this.maDe = maDe;
        this.cauDung = cauDung;
        this.diem = diem;
    }

    public String getMaTS() { return maTS; }
    public String getMaDe() { return maDe; }
    public String getCauDung() { return cauDung; }
    public String getDiem() { return diem; }
}