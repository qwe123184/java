package com.fenglin.springboottest.common;

import java.io.Serializable;
import java.util.List;

/**
 * 个人中心一站式总览：一次返回资料 / 试炼 / 战绩 / 等级 / 成就，减少前端多次往返。
 */
public class MeOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserVO user;
    private StageProgressVO stage;
    private GameStatVO stat;

    private int level;
    private String levelTitle;
    private int levelPercent;
    private int expToNext;

    private List<AchievementVO> achievements;
    private int unlockedCount;
    private int totalCount;

    public MeOverviewVO() {
    }

    public UserVO getUser() { return user; }
    public void setUser(UserVO user) { this.user = user; }
    public StageProgressVO getStage() { return stage; }
    public void setStage(StageProgressVO stage) { this.stage = stage; }
    public GameStatVO getStat() { return stat; }
    public void setStat(GameStatVO stat) { this.stat = stat; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getLevelTitle() { return levelTitle; }
    public void setLevelTitle(String levelTitle) { this.levelTitle = levelTitle; }
    public int getLevelPercent() { return levelPercent; }
    public void setLevelPercent(int levelPercent) { this.levelPercent = levelPercent; }
    public int getExpToNext() { return expToNext; }
    public void setExpToNext(int expToNext) { this.expToNext = expToNext; }
    public List<AchievementVO> getAchievements() { return achievements; }
    public void setAchievements(List<AchievementVO> achievements) { this.achievements = achievements; }
    public int getUnlockedCount() { return unlockedCount; }
    public void setUnlockedCount(int unlockedCount) { this.unlockedCount = unlockedCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
