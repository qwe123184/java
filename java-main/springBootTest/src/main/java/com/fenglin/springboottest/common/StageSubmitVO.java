package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 提交试炼后的结果：判定是否突破、更新后的进度与叙事文案。
 */
public class StageSubmitVO {

    private boolean passed;
    private int correctCount;
    private int total;
    private int passThreshold;
    private int breakthroughValue;  // 本次答对折算的突破值（答对题数 × 20）
    private String realmKey;
    private String realmName;
    private String nextRealmKey;
    private String nextRealmName;
    private boolean allCleared;
    private int clears;
    private int exp;
    private int bestStreak;
    private String message;

    /** 每题作答解析（正确答案 vs 玩家选择），用于结果页复盘 */
    private List<AnswerReview> review;

    public static class AnswerReview {
        private Long questionId;
        private String question;
        private String yourChoice;      // 玩家选择，未作答为 null
        private String correctAnswer;   // 正确答案 A/B/C/D
        private boolean correct;

        public AnswerReview() {
        }

        public AnswerReview(Long questionId, String question, String yourChoice, String correctAnswer, boolean correct) {
            this.questionId = questionId;
            this.question = question;
            this.yourChoice = yourChoice;
            this.correctAnswer = correctAnswer;
            this.correct = correct;
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getYourChoice() { return yourChoice; }
        public void setYourChoice(String yourChoice) { this.yourChoice = yourChoice; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }

    public List<AnswerReview> getReview() { return review; }
    public void setReview(List<AnswerReview> review) { this.review = review; }

    public StageSubmitVO() {
    }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int passThreshold) { this.passThreshold = passThreshold; }
    public int getBreakthroughValue() { return breakthroughValue; }
    public void setBreakthroughValue(int breakthroughValue) { this.breakthroughValue = breakthroughValue; }
    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public String getRealmName() { return realmName; }
    public void setRealmName(String realmName) { this.realmName = realmName; }
    public String getNextRealmKey() { return nextRealmKey; }
    public void setNextRealmKey(String nextRealmKey) { this.nextRealmKey = nextRealmKey; }
    public String getNextRealmName() { return nextRealmName; }
    public void setNextRealmName(String nextRealmName) { this.nextRealmName = nextRealmName; }
    public boolean isAllCleared() { return allCleared; }
    public void setAllCleared(boolean allCleared) { this.allCleared = allCleared; }
    public int getClears() { return clears; }
    public void setClears(int clears) { this.clears = clears; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
