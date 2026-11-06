package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发起对决擂台请求
 */
public class DuelCreateRequest {

    /** 对手用户名（被挑战者） */
    @NotBlank(message = "请指定对手代号。")
    private String opponentUsername;

    /** 主题：shenyu / tomjerry */
    private String theme = "shenyu";

    /** 最佳 N 胜（2~5） */
    @NotNull(message = "请设定目标胜场。")
    private Integer targetWins = 3;

    public DuelCreateRequest() {
    }

    public String getOpponentUsername() { return opponentUsername; }
    public void setOpponentUsername(String opponentUsername) { this.opponentUsername = opponentUsername; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getTargetWins() { return targetWins; }
    public void setTargetWins(Integer targetWins) { this.targetWins = targetWins; }
}
