package com.fenglin.springboottest.common;

import com.fenglin.springboottest.entity.DuelRound;

import java.util.List;

/**
 * 对决擂台房间视图对象（含回合列表与参与者昵称）
 */
public class DuelRoomVO {

    private Long id;
    private String roomCode;
    private String theme;
    private String status;
    private Integer currentRound;
    private Integer p1Wins;
    private Integer p2Wins;
    private Integer targetWins;
    private Long winnerId;
    /** 胜者角色：P1 / P2 / null（便于前端判定“我方是否获胜”） */
    private String winnerRole;
    private String challengerName;
    private String opponentName;
    /** 当前用户在房间中的角色：P1 / P2 */
    private String myRole;
    /** 当前回合我已提交的出手（若已提交） */
    private String myChoice;
    /** 对手在当前回合已提交的出手（若已提交且我方也已提交则可见，否则 null） */
    private String oppChoice;
    private List<DuelRound> rounds;

    public DuelRoomVO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCurrentRound() { return currentRound; }
    public void setCurrentRound(Integer currentRound) { this.currentRound = currentRound; }
    public Integer getP1Wins() { return p1Wins; }
    public void setP1Wins(Integer p1Wins) { this.p1Wins = p1Wins; }
    public Integer getP2Wins() { return p2Wins; }
    public void setP2Wins(Integer p2Wins) { this.p2Wins = p2Wins; }
    public Integer getTargetWins() { return targetWins; }
    public void setTargetWins(Integer targetWins) { this.targetWins = targetWins; }
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
    public String getMyChoice() { return myChoice; }
    public void setMyChoice(String myChoice) { this.myChoice = myChoice; }
    public String getOppChoice() { return oppChoice; }
    public void setOppChoice(String oppChoice) { this.oppChoice = oppChoice; }
    public List<DuelRound> getRounds() { return rounds; }
    public void setRounds(List<DuelRound> rounds) { this.rounds = rounds; }
}
