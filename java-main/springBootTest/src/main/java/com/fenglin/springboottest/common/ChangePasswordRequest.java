package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改密码请求（明文比对旧密码，明文写入新密码，按需求不加密）
 */
public class ChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 原密码（明文） */
    @NotBlank(message = "请先输入原禁墟密钥。")
    private String oldPassword;

    /** 新密码（明文，至少 4 位） */
    @NotBlank(message = "新禁墟密钥不可为空。")
    @Size(min = 4, max = 64, message = "新密钥至少需 4 位。")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
