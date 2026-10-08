package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 守夜人账户实体，对应数据表 zhan_shen.t_user
 * （表名带库名前缀，配合“无默认库”的数据源 URL，使建库建表可由应用启动自动完成）
 */
@TableName("zhan_shen.t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 守夜人代号（登录名，唯一） */
    private String username;

    /** 禁墟密钥（BCrypt 加密存储） */
    private String password;

    /** 灵魂烙印（邮箱，可空） */
    private String email;

    /** 禁墟境界：盏境 / 池境 / 川境 / 海境 */
    private String realm;

    /** 账号状态：0 正常 / 1 已封禁 */
    private Integer status;

    /** 注册时间 */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /** 最后更新时间（由数据库 ON UPDATE 维护） */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public User() {
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
