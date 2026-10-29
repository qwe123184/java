package com.fenglin.springboottest.common;

import com.fenglin.springboottest.entity.Archive;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 神域档案对外视图对象（描述字段映射为前端约定的 desc）
 */
public class ArchiveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String realm;
    /** 前端卡片使用 it.desc 读取描述 */
    private String desc;
    /** 封面图 URL（可空） */
    private String cover;
    /** 标签（逗号分隔，可空） */
    private String tags;
    /** 排序值（越大越靠前），前端用于「排行」页战力条 */
    private Integer sort;
    /** 展示状态：0 下架 / 1 展示 */
    private Integer status;
    /** 创建时间（后台管理展示用） */
    private LocalDateTime createTime;

    public static ArchiveVO fromEntity(Archive a) {
        if (a == null) {
            return null;
        }
        ArchiveVO vo = new ArchiveVO();
        vo.setId(a.getId());
        vo.setName(a.getName());
        vo.setType(a.getType());
        vo.setRealm(a.getRealm());
        vo.setDesc(a.getDescription());
        vo.setCover(a.getCover());
        vo.setTags(a.getTags());
        vo.setSort(a.getSort());
        vo.setStatus(a.getStatus());
        vo.setCreateTime(a.getCreateTime());
        return vo;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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
