package com.fenglin.springboottest.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册请求
 */
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 守夜人代号（登录名） */
    @NotBlank(message = "守夜人代号不可为空。")
    @Size(min = 2, max = 32, message = "守夜人代号长度需在 2–32 位之间。")
    private String username;

    /** 禁墟密钥（明文，后端加密存储） */
    @NotBlank(message = "禁墟密钥不可为空。")
    @Size(min = 4, max = 64, message = "禁墟密钥至少需 4 位，横刀之誓不可轻许。")
    private String password;

    /** 灵魂烙印（邮箱，可选） */
    @Email(message = "灵魂烙印（邮箱）格式有误。")
    private String email;

    /** 禁墟境界：盏境 / 池境 / 川境 / 海境 */
    private String realm;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
