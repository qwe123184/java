package com.fenglin.springboottest.common;

import com.fenglin.springboottest.entity.Archive;

import java.io.Serializable;

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
}
