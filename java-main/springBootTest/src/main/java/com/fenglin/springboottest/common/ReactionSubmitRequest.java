package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotNull;

/**
 * 反应试炼提交请求，ms 为本次反应耗时（毫秒）
 */
public class ReactionSubmitRequest {

    @NotNull(message = "请提交本次反应耗时。")
    private Integer ms;

    public ReactionSubmitRequest() {
    }

    public Integer getMs() { return ms; }
    public void setMs(Integer ms) { this.ms = ms; }
}
