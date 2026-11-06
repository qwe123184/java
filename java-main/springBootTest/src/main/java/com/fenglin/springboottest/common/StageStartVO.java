package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 开始一次境界试炼的响应：返回本关题目（不含正确答案）与闯关规则。
 */
public class StageStartVO {

    private String realmKey;
    private String realmName;
    private int questions;
    private int passThreshold;
    private String desc;
    /** 本关题目（不含 correctAnswer） */
    private List<QuizQuestionVO> items;
    /** 该境界是否已被突破过（可重复挑战刷战绩） */
    private boolean alreadyCleared;

    public StageStartVO() {
    }

    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public String getRealmName() { return realmName; }
    public void setRealmName(String realmName) { this.realmName = realmName; }
    public int getQuestions() { return questions; }
    public void setQuestions(int questions) { this.questions = questions; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int passThreshold) { this.passThreshold = passThreshold; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public List<QuizQuestionVO> getItems() { return items; }
    public void setItems(List<QuizQuestionVO> items) { this.items = items; }
    public boolean isAlreadyCleared() { return alreadyCleared; }
    public void setAlreadyCleared(boolean alreadyCleared) { this.alreadyCleared = alreadyCleared; }
}
