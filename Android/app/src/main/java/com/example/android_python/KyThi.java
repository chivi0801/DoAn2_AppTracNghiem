package com.example.android_python;

public class KyThi {
    private int examId;
    private String subject, date, sheetType;

    public KyThi(int examId, String subject, String date, String sheetType) {
        this.examId = examId;
        this.subject = subject;
        this.date = date;
        this.sheetType = sheetType;
    }

    // Getters
    public int getExamId() { return examId; }
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getSheetType() { return sheetType; }

    // Setters
    public void setSubject(String subject) { this.subject = subject; }
    public void setSheetType(String sheetType) { this.sheetType = sheetType; }
}