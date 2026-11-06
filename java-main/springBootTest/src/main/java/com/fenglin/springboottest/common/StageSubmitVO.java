package com.fenglin.springboottest.common;

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
