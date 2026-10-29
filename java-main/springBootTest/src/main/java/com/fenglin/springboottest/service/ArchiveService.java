package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fenglin.springboottest.common.ArchiveSaveRequest;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;

/**
 * 神域档案业务接口（首页展示 / 检索 / 详情 / 后台管理）
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

    /**
     * 后台档案库：分页检索全部档案（含下架），支持关键词 / 类型 / 状态筛选。
     */
    Result<IPage<ArchiveVO>> listAll(String keyword, String type, Integer status, long page, long size);

    /**
     * 新增档案（后台管理）；成功返回新建档案视图
     */
    Result<ArchiveVO> create(ArchiveSaveRequest req);

    /**
     * 修改档案（后台管理）；id 不存在返回失败
     */
    Result<ArchiveVO> update(Long id, ArchiveSaveRequest req);

    /**
     * 删除档案（后台管理）
     */
    Result<Void> remove(Long id);

    /**
     * 批量上下架（后台管理）：按 id 列表统一设置状态（0 下架 / 1 上架）
     */
    Result<Void> batchStatus(java.util.List<Long> ids, Integer status);
}
