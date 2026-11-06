package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 答题对战房间实体，对应数据表 zhan_shen.t_quiz_room
 */
@TableName("zhan_shen.t_quiz_room")
public class QuizRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomCode;

    private Long challengerId;

    private Long opponentId;

    private String theme;

    /** WAITING / PLAYING / FINISHED / CANCELED */
    private String status;

    /** 本局题目数量 */
    private Integer qCount;

    /** JSON 数组，本局抽中的题目 id */
    private String questionIds;

    private Integer p1Score;

    private Integer p2Score;

    private Long winnerId;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "finished_at")
    private LocalDateTime finishedAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public QuizRoom() {
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
    public Integer getQCount() { return qCount; }
    public void setQCount(Integer qCount) { this.qCount = qCount; }
    public String getQuestionIds() { return questionIds; }
    public void setQuestionIds(String questionIds) { this.questionIds = questionIds; }
    public Integer getP1Score() { return p1Score; }
    public void setP1Score(Integer p1Score) { this.p1Score = p1Score; }
    public Integer getP2Score() { return p2Score; }
    public void setP2Score(Integer p2Score) { this.p2Score = p2Score; }
    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
