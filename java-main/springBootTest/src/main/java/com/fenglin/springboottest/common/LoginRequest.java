package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 登录请求
 */
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 守夜人代号（登录名） */
    @NotBlank(message = "请先留下你的守夜人代号。")
    @Size(min = 2, max = 32, message = "守夜人代号长度需在 2–32 位之间。")
    private String username;

    /** 禁墟密钥（明文） */
    @NotBlank(message = "请先留下你的禁墟密钥。")
    @Size(min = 4, max = 64, message = "禁墟密钥格式有误。")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
