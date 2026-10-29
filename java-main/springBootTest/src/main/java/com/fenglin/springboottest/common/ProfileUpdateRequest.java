package com.fenglin.springboottest.common;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改个人资料请求（仅可修改自身展示信息，不含账号/密码/权限）
 */
public class ProfileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 昵称（展示用，可空） */
    @Size(max = 50, message = "昵称长度不可超过 50 字。")
    private String nickname;

    /** 头像 URL（可空） */
    @Size(max = 255, message = "头像地址过长。")
    private String avatar;

    /** 手机号（可空） */
    @Size(max = 20, message = "手机号格式有误。")
    private String phone;

    /** 个人简介（可空） */
    @Size(max = 500, message = "简介长度不可超过 500 字。")
    private String bio;

    /** 邮箱（可空） */
    @Size(max = 120, message = "邮箱过长。")
    private String email;

    /** 禁墟境界：盏境 / 池境 / 川境 / 海境 */
    private String realm;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
