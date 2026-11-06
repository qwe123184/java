package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户闯关进度实体，对应数据表 zhan_shen.t_user_stage（与 t_user 一对一）。
 * current_realm：当前正在挑战的境界 key（尚未突破）。
 * max_realm：已突破到的最高境界 key（null 表示尚无一境突破）。
 */
@TableName("zhan_shen.t_user_stage")
public class UserStage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String currentRealm;

    private String maxRealm;

    /** 累计突破次数（每次成功突破 +1，含重复挑战已突破的境界） */
    private Integer clears = 0;

    /** 经验值：每答对一题 +10 */
    private Integer exp = 0;

    /** 单次闯关中连续答对的最高纪录 */
    private Integer bestStreak = 0;

    @com.baomidou.mybatisplus.annotation.TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public UserStage() {
    }

    public UserStage(Long userId) {
        this.userId = userId;
        this.currentRealm = "fanchen";
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCurrentRealm() { return currentRealm; }
    public void setCurrentRealm(String currentRealm) { this.currentRealm = currentRealm; }
    public String getMaxRealm() { return maxRealm; }
    public void setMaxRealm(String maxRealm) { this.maxRealm = maxRealm; }
    public Integer getClears() { return clears; }
    public void setClears(Integer clears) { this.clears = clears; }
    public Integer getExp() { return exp; }
    public void setExp(Integer exp) { this.exp = exp; }
    public Integer getBestStreak() { return bestStreak; }
    public void setBestStreak(Integer bestStreak) { this.bestStreak = bestStreak; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
