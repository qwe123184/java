package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户成就解锁记录，对应数据表 zhan_shen.t_user_achievement。
 * 主键为自增 id，(user_id, achievement_key) 唯一约束防止重复解锁。
 */
@TableName("zhan_shen.t_user_achievement")
public class UserAchievement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String achievementKey;
    private LocalDateTime unlockedAt;

    public UserAchievement() {
    }

    public UserAchievement(Long userId, String achievementKey) {
        this.userId = userId;
        this.achievementKey = achievementKey;
        this.unlockedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAchievementKey() { return achievementKey; }
    public void setAchievementKey(String achievementKey) { this.achievementKey = achievementKey; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
