package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 答题记录实体，对应数据表 zhan_shen.t_quiz_answer
 */
@TableName("zhan_shen.t_quiz_answer")
public class QuizAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Long userId;

    private Long questionId;

    /** A/B/C/D */
    private String choice;

    /** 0 错 / 1 对 */
    private Integer correct;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    public QuizAnswer() {
    }

    public QuizAnswer(Long roomId, Long userId, Long questionId, String choice, Integer correct) {
        this.roomId = roomId;
        this.userId = userId;
        this.questionId = questionId;
        this.choice = choice;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
    public Integer getCorrect() { return correct; }
    public void setCorrect(Integer correct) { this.correct = correct; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
