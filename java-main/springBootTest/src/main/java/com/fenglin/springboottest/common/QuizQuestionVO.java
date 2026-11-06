package com.fenglin.springboottest.common;

/**
 * 答题对战中的单题视图（正确答案仅在房间结束后才下发，避免泄题）
 */
public class QuizQuestionVO {

    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    /** 我方该题作答（未答为 null） */
    private String myChoice;
    /** 正确答案（仅房间 FINISHED 后下发） */
    private String correctAnswer;
    /** 该题是否已作答 */
    private boolean answered;

    public QuizQuestionVO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getMyChoice() { return myChoice; }
    public void setMyChoice(String myChoice) { this.myChoice = myChoice; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public boolean isAnswered() { return answered; }
    public void setAnswered(boolean answered) { this.answered = answered; }
}
