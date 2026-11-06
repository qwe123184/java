package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对决擂台房间实体，对应数据表 zhan_shen.t_duel_room
 */
@TableName("zhan_shen.t_duel_room")
public class DuelRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间邀请码 */
    private String roomCode;

    /** 发起者（P1） */
    private Long challengerId;

    /** 被挑战者（P2） */
    private Long opponentId;

    /** 主题：shenyu 神域 / tomjerry 猫鼠 */
    private String theme;

    /** 状态：WAITING / PLAYING / FINISHED / CANCELED */
    private String status;

    /** 当前回合（从 1 开始） */
    private Integer currentRound;

    /** P1 已胜回合 */
    private Integer p1Wins;

    /** P2 已胜回合 */
    private Integer p2Wins;

    /** 先达此胜场者获胜 */
    private Integer targetWins;

    /** 胜者 id（FINISHED 后填入） */
    private Long winnerId;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "finished_at")
    private LocalDateTime finishedAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public DuelRoom() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public Long getChallengerId() { return challengerId; }
    public void setChallengerId(Long challengerId) { this.challengerId = challengerId; }
    public Long getOpponentId() { return opponentId; }
    public void setOpponentId(Long opponentId) { this.opponentId = opponentId; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCurrentRound() { return currentRound; }
    public void setCurrentRound(Integer currentRound) { this.currentRound = currentRound; }
    public Integer getP1Wins() { return p1Wins; }
    public void setP1Wins(Integer p1Wins) { this.p1Wins = p1Wins; }
    public Integer getP2Wins() { return p2Wins; }
    public void setP2Wins(Integer p2Wins) { this.p2Wins = p2Wins; }
    public Integer getTargetWins() { return targetWins; }
    public void setTargetWins(Integer targetWins) { this.targetWins = targetWins; }
    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
