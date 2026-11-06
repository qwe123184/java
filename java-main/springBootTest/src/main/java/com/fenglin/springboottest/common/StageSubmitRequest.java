package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 提交一次境界试炼的答案。
 * answers 中每项含 questionId 与 choice（A/B/C/D），由后端比对正解除泄题风险。
 */
public class StageSubmitRequest {

    private String realmKey;
    private List<StageAnswer> answers;

    public static class StageAnswer {
        private Long questionId;
        private String choice;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getChoice() { return choice; }
        public void setChoice(String choice) { this.choice = choice; }
    }

    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public List<StageAnswer> getAnswers() { return answers; }
    public void setAnswers(List<StageAnswer> answers) { this.answers = answers; }
}
