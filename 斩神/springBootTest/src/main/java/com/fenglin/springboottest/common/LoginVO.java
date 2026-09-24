package com.fenglin.springboottest.common;

import java.io.Serializable;

/**
 * 登录响应：会话令牌 + 用户信息
 */
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话令牌（前端持久化到 localStorage） */
    private String token;

    /** 当前守夜人信息 */
    private UserVO user;

    public LoginVO() {
    }

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
