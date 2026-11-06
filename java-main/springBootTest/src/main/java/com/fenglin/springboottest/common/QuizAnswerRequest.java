package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 答题对战作答请求，choice ∈ {A, B, C, D}
 * questionId 可选：不传则由服务端按当前进度推断下一题。
 */
public class QuizAnswerRequest {

    @NotBlank(message = "请选择你的答案。")
    private String choice;

    private Long questionId;

    public QuizAnswerRequest() {
    }

    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
}
