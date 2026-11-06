package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发起禁墟答题对战请求
 */
public class QuizCreateRequest {

    @NotBlank(message = "请指定对手代号。")
    private String opponentUsername;

    private String theme = "shenyu";

    /** 题目数量（3~10） */
    @NotNull(message = "请设定题目数量。")
    private Integer count = 5;

    public QuizCreateRequest() {
    }

    public String getOpponentUsername() { return opponentUsername; }
    public void setOpponentUsername(String opponentUsername) { this.opponentUsername = opponentUsername; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
