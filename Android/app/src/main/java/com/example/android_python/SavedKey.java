package com.example.android_python;

import java.io.Serializable;

// Cần implements Serializable để gửi được cả Object qua Bundle
public class SavedKey implements Serializable {
    private String maDe;
    private String dapAn;

    public SavedKey(String maDe, String dapAn) {
        this.maDe = maDe;
        this.dapAn = dapAn;
    }

    public String getMaDe() { return maDe; }
    public String getDapAn() { return dapAn; }
}