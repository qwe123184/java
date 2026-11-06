package com.fenglin.springboottest.common;

/**
 * 闯关进度视图：前端用于渲染「境界地图 / 进度条」。
 */
public class StageProgressVO {

    private String currentRealmKey;
    private String currentRealmName;
    private int currentRealmIndex;  // 0-based
    private int totalRealms;
    private int currentQuestions;
    private int passThreshold;
    private String realmDesc;

    private String maxRealmKey;
    private String maxRealmName;
    private boolean allCleared;

    private int clears;
    private int exp;
    private int bestStreak;
    private int progressPercent;    // 基于已突破境界，0~100

    public StageProgressVO() {
    }

    public String getCurrentRealmKey() { return currentRealmKey; }
    public void setCurrentRealmKey(String currentRealmKey) { this.currentRealmKey = currentRealmKey; }
    public String getCurrentRealmName() { return currentRealmName; }
    public void setCurrentRealmName(String currentRealmName) { this.currentRealmName = currentRealmName; }
    public int getCurrentRealmIndex() { return currentRealmIndex; }
    public void setCurrentRealmIndex(int currentRealmIndex) { this.currentRealmIndex = currentRealmIndex; }
    public int getTotalRealms() { return totalRealms; }
    public void setTotalRealms(int totalRealms) { this.totalRealms = totalRealms; }
    public int getCurrentQuestions() { return currentQuestions; }
    public void setCurrentQuestions(int currentQuestions) { this.currentQuestions = currentQuestions; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int passThreshold) { this.passThreshold = passThreshold; }
    public String getRealmDesc() { return realmDesc; }
    public void setRealmDesc(String realmDesc) { this.realmDesc = realmDesc; }
    public String getMaxRealmKey() { return maxRealmKey; }
    public void setMaxRealmKey(String maxRealmKey) { this.maxRealmKey = maxRealmKey; }
    public String getMaxRealmName() { return maxRealmName; }
    public void setMaxRealmName(String maxRealmName) { this.maxRealmName = maxRealmName; }
    public boolean isAllCleared() { return allCleared; }
    public void setAllCleared(boolean allCleared) { this.allCleared = allCleared; }
    public int getClears() { return clears; }
    public void setClears(int clears) { this.clears = clears; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
}
