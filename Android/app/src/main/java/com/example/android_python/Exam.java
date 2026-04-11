package com.example.android_python;

public class Exam {
    private int examId; // Thêm biến này
    private String subject, date, sheetType;
    private int questionCount;

    public Exam(int examId, String subject, String date, String sheetType, int questionCount) {
        this.examId = examId;
        this.subject = subject;
        this.date = date;
        this.sheetType = sheetType;
        this.questionCount = questionCount;
    }

    // Getters
    public int getExamId() { return examId; } // Thêm getter cho ID
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getSheetType() { return sheetType; }
    public int getQuestionCount() { return questionCount; }
}