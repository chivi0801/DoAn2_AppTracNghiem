package com.example.android_python;

public class Exam {
    private int examId;
    private String subject, date, sheetType;

    private int questionCount;

    public Exam(int examId, String subject, String date, String sheetType) {
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
    public int getQuestionCount() {
        return questionCount;
    }
}