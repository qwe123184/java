package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对决回合实体，对应数据表 zhan_shen.t_duel_round
 */
@TableName("zhan_shen.t_duel_round")
public class DuelRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Integer roundNo;

    /** ATTACK / DEFEND / SKILL */
    private String p1Choice;

    private String p2Choice;

    /** P1 / P2 / DRAW / null(未结算) */
    private String winner;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    public DuelRound() {
    }

    public DuelRound(Long roomId, Integer roundNo) {
        this.roomId = roomId;
        this.roundNo = roundNo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Integer getRoundNo() { return roundNo; }
    public void setRoundNo(Integer roundNo) { this.roundNo = roundNo; }
    public String getP1Choice() { return p1Choice; }
    public void setP1Choice(String p1Choice) { this.p1Choice = p1Choice; }
    public String getP2Choice() { return p2Choice; }
    public void setP2Choice(String p2Choice) { this.p2Choice = p2Choice; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
