package com.fenglin.springboottest.common;

import java.io.Serializable;

/**
 * 成就视图对象：对外展示一个成就的解锁状态与进度。
 */
public class AchievementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String name;
    private String desc;
    private String icon;
    private String tier;          // COMMON / RARE / EPIC / LEGEND
    private String goal;          // 人类可读的达成目标，如 “累计突破 1 次”
    private boolean unlocked;     // 是否已解锁
    private String unlockedAt;    // 解锁时间（ISO 字符串），未解锁为 null
    private boolean isNew;        // 解锁且在 24 小时内（前端展示 NEW 角标）
    private String progress;      // 进度文案，如 “已突破 2/5 次” 或 “暂无记录”

    public AchievementVO() {
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public String getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    public String getProgress() { return progress; }
    public void setProgress(String progress) { this.progress = progress; }
}
