package com.fenglin.springboottest.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 档案新增 / 修改请求（后台管理）
 */
public class ArchiveSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 名称（禁墟之名 / 神秘之名 / 守夜人代号 / 任务名） */
    @NotBlank(message = "档案名称不可为空。")
    @Size(max = 64, message = "档案名称长度不可超过 64 字。")
    private String name;

    /** 类型：禁墟 / 神秘 / 守夜人 / 任务 */
    @NotBlank(message = "档案类型不可为空。")
    @Size(max = 16, message = "档案类型长度不可超过 16 字。")
    private String type;

    /** 境界 / 等级（可选，如 无量、克莱因、海境、夜幕） */
    @Size(max = 32, message = "境界长度不可超过 32 字。")
    private String realm;

    /** 描述（可选） */
    @Size(max = 2000, message = "描述长度不可超过 2000 字。")
    private String description;

    /** 展示状态：0 下架 / 1 展示（不传默认 1） */
    private Integer status;

    /** 排序（越大越靠前，不传默认 0） */
    private Integer sort;

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
}
