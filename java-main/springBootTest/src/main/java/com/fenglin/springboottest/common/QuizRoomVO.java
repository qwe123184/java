package com.fenglin.springboottest.common;

import java.util.List;

/**
 * 答题对战房间视图对象
 */
public class QuizRoomVO {

    private Long id;
    private String roomCode;
    private String theme;
    private String status;
    private Integer qCount;
    private Long winnerId;
    /** 胜者角色：P1 / P2 / null */
    private String winnerRole;
    private String challengerName;
    private String opponentName;
    private String myRole;
    private Integer myScore;
    private Integer oppScore;
    /** 我方已答题数 / 对手已答题数 */
    private Integer myProgress;
    private Integer oppProgress;
    private List<QuizQuestionVO> questions;

    public QuizRoomVO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getQCount() { return qCount; }
    public void setQCount(Integer qCount) { this.qCount = qCount; }
    public Long getWinnerId() { return winnerId; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
    public String getWinnerRole() { return winnerRole; }
    public void setWinnerRole(String winnerRole) { this.winnerRole = winnerRole; }
    public String getChallengerName() { return challengerName; }
    public void setChallengerName(String challengerName) { this.challengerName = challengerName; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public String getMyRole() { return myRole; }
    public void setMyRole(String myRole) { this.myRole = myRole; }
    public Integer getMyScore() { return myScore; }
    public void setMyScore(Integer myScore) { this.myScore = myScore; }
    public Integer getOppScore() { return oppScore; }
    public void setOppScore(Integer oppScore) { this.oppScore = oppScore; }
    public Integer getMyProgress() { return myProgress; }
    public void setMyProgress(Integer myProgress) { this.myProgress = myProgress; }
    public Integer getOppProgress() { return oppProgress; }
    public void setOppProgress(Integer oppProgress) { this.oppProgress = oppProgress; }
    public List<QuizQuestionVO> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestionVO> questions) { this.questions = questions; }
}
