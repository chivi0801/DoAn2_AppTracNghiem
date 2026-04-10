package com.example.android_python;

public class Exam {
    private String subject, date, sheetType;
    private int questionCount;

    public Exam(String subject, String date, String sheetType, int questionCount) {
        this.subject = subject;
        this.date = date;
        this.sheetType = sheetType;
        this.questionCount = questionCount;
    }
    // Getters
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getSheetType() { return sheetType; }
    public int getQuestionCount() { return questionCount; }
}