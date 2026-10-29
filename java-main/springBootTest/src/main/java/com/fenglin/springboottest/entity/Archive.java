package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 神域档案实体，对应数据表 zhan_shen.t_archive
 * （首页展示 / 检索的数据源：禁墟、神秘、守夜人、任务指令等）
 */
@TableName("zhan_shen.t_archive")
public class Archive implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称（禁墟之名 / 神秘之名 / 守夜人代号 / 任务名） */
    private String name;

    /** 类型：禁墟 / 神秘 / 守夜人 / 任务 */
    private String type;

    /** 境界 / 等级（如 无量、克莱因、海境、夜幕） */
    private String realm;

    /** 描述 */
    private String description;

    /** 封面图 URL（可空） */
    private String cover;

    /** 标签（逗号分隔，可空） */
    private String tags;

    /** 展示状态：0 下架 / 1 展示（首页只展示=1 的） */
    private Integer status;

    /** 排序（越大越靠前） */
    private Integer sort;

    /** 创建时间 */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /** 最后更新时间（由数据库 ON UPDATE 维护） */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public Archive() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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
