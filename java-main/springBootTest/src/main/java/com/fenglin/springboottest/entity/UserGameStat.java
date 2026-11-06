package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户游戏战绩汇总实体，对应数据表 zhan_shen.t_user_game_stat
 * 主键为 user_id（与 t_user 一对一），用于三款游戏的跨用户排行。
 */
@TableName("zhan_shen.t_user_game_stat")
public class UserGameStat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long userId;

    // 所有计数字段默认 0（0 同时表示“尚无记录”，与业务层语义一致），
    // 避免 ensureStat 插入新行后返回内存对象字段为 null 导致 recordDuel/recordQuiz/submitReaction 中出现 NPE。
    private Integer duelWins = 0;
    private Integer duelLosses = 0;
    private Integer duelDraws = 0;

    private Integer quizWins = 0;
    private Integer quizLosses = 0;
    private Integer quizBestScore = 0;

    /** 反应试炼最佳毫秒（0 表示尚无记录） */
    private Integer reactionBestMs = 0;
    private Integer reactionPlays = 0;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public UserGameStat() {
    }

    public UserGameStat(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getDuelWins() { return duelWins; }
    public void setDuelWins(Integer duelWins) { this.duelWins = duelWins; }
    public Integer getDuelLosses() { return duelLosses; }
    public void setDuelLosses(Integer duelLosses) { this.duelLosses = duelLosses; }
    public Integer getDuelDraws() { return duelDraws; }
    public void setDuelDraws(Integer duelDraws) { this.duelDraws = duelDraws; }
    public Integer getQuizWins() { return quizWins; }
    public void setQuizWins(Integer quizWins) { this.quizWins = quizWins; }
    public Integer getQuizLosses() { return quizLosses; }
    public void setQuizLosses(Integer quizLosses) { this.quizLosses = quizLosses; }
    public Integer getQuizBestScore() { return quizBestScore; }
    public void setQuizBestScore(Integer quizBestScore) { this.quizBestScore = quizBestScore; }
    public Integer getReactionBestMs() { return reactionBestMs; }
    public void setReactionBestMs(Integer reactionBestMs) { this.reactionBestMs = reactionBestMs; }
    public Integer getReactionPlays() { return reactionPlays; }
    public void setReactionPlays(Integer reactionPlays) { this.reactionPlays = reactionPlays; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
