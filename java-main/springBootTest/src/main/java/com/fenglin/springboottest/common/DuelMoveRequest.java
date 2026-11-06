package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 对决回合出招请求，choice ∈ {ATTACK, DEFEND, SKILL}
 */
public class DuelMoveRequest {

    @NotBlank(message = "请选择你的出手：攻/防/术。")
    private String choice;

    public DuelMoveRequest() {
    }

    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
}
