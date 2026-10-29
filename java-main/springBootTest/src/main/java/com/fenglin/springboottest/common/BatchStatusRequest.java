package com.fenglin.springboottest.common;

import java.io.Serializable;
import java.util.List;

/**
 * 批量上下架请求：ids 档案编号列表，status 目标状态（0 下架 / 1 上架）
 */
public class BatchStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> ids;
    private Integer status;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
