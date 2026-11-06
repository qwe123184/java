package com.fenglin.springboottest.common;

/**
 * 通用排行榜视图对象
 * score 为该榜单主指标；sub 为附加说明（如「X胜Y负」「最佳 X 分」）。
 */
public class LeaderboardVO {

    private int rank;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private int score;
    private String sub;

    public LeaderboardVO() {
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
}
