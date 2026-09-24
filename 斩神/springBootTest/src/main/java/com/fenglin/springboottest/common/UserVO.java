package com.fenglin.springboottest.common;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户对外视图对象（不含密码等敏感字段）
 */
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;
    private String realm;
    private Integer status;
    private LocalDateTime createTime;

    public static UserVO fromEntity(com.fenglin.springboottest.entity.User u) {
        if (u == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setEmail(u.getEmail());
        vo.setRealm(u.getRealm());
        vo.setStatus(u.getStatus());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
