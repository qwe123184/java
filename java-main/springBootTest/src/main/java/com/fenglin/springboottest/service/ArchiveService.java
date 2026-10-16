package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;

/**
 * 神域档案业务接口（首页展示 / 检索 / 详情）
 */
public interface ArchiveService {

    /**
     * 分页检索神域档案（首页展示 + 搜索）。
     * keyword 为空时返回全部展示中档案；type 为空或"全部"时不过滤类型。
     */
    Result<IPage<ArchiveVO>> search(String keyword, String type, long page, long size);

    /**
     * 按 ID 查询单条档案详情；不存在返回失败
     */
    Result<ArchiveVO> detail(Long id);
}
