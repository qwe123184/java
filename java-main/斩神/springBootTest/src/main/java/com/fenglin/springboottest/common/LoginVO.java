package com.fenglin.springboottest.common;

import java.io.Serializable;

/**
 * 登录响应：双令牌 + 用户信息
 */
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌（= accessToken），前端日常调用接口使用 */
    private String token;

    /** 访问令牌（语义化别名，与 token 等价） */
    private String accessToken;

    /** 刷新令牌，用于 /api/user/refresh 续期 */
    private String refreshToken;

    /** 当前守夜人信息 */
    private UserVO user;

    public LoginVO() {
    }

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.accessToken = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
