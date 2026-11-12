package com.fenglin.springboottest.common;

import java.io.Serializable;

/**
 * 用户三款小游戏（对决 / 答题 / 反应）的战绩汇总视图对象。
 */
public class GameStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int duelWins;
    private int duelLosses;
    private int duelDraws;
    private int quizWins;
    private int quizLosses;
    private int quizBestScore;
    private int reactionBestMs;   // 0 表示尚无记录
    private int reactionPlays;

    public GameStatVO() {
    }

    public int getDuelWins() { return duelWins; }
    public void setDuelWins(int duelWins) { this.duelWins = duelWins; }
    public int getDuelLosses() { return duelLosses; }
    public void setDuelLosses(int duelLosses) { this.duelLosses = duelLosses; }
    public int getDuelDraws() { return duelDraws; }
    public void setDuelDraws(int duelDraws) { this.duelDraws = duelDraws; }
    public int getQuizWins() { return quizWins; }
    public void setQuizWins(int quizWins) { this.quizWins = quizWins; }
    public int getQuizLosses() { return quizLosses; }
    public void setQuizLosses(int quizLosses) { this.quizLosses = quizLosses; }
    public int getQuizBestScore() { return quizBestScore; }
    public void setQuizBestScore(int quizBestScore) { this.quizBestScore = quizBestScore; }
    public int getReactionBestMs() { return reactionBestMs; }
    public void setReactionBestMs(int reactionBestMs) { this.reactionBestMs = reactionBestMs; }
    public int getReactionPlays() { return reactionPlays; }
    public void setReactionPlays(int reactionPlays) { this.reactionPlays = reactionPlays; }
}
