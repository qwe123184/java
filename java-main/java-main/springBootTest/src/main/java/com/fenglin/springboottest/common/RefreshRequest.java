package com.fenglin.springboottest.common;

import java.io.Serializable;

/**
 * 刷新令牌请求
 */
public class RefreshRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 刷新令牌 */
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
