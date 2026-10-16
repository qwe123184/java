package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Archive;
import com.fenglin.springboottest.mapper.ArchiveMapper;
import com.fenglin.springboottest.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 神域档案业务实现
 */
@Service
public class ArchiveServiceImpl implements ArchiveService {

    /** 前端"全部"分类的占位值，后端收到即视为不过滤类型 */
    private static final String ALL = "全部";

    @Autowired
    private ArchiveMapper archiveMapper;

    @Override
    public Result<IPage<ArchiveVO>> search(String keyword, String type, long page, long size) {
        LambdaQueryWrapper<Archive> qw = new LambdaQueryWrapper<>();
        // 首页只展示 status=1 的档案
        qw.eq(Archive::getStatus, 1);

        // 类型筛选（空 / "全部" 不过滤）
        if (StringUtils.hasText(type) && !ALL.equals(type.trim())) {
            qw.eq(Archive::getType, type.trim());
        }

        // 关键词检索：名称 / 描述 / 境界 / 类型
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            qw.and(w -> w.like(Archive::getName, k)
                    .or().like(Archive::getDescription, k)
                    .or().like(Archive::getRealm, k)
                    .or().like(Archive::getType, k));
        }

        // 排序：sort 越大越靠前，其次按 id 倒序（新录入的优先）
        qw.orderByDesc(Archive::getSort).orderByDesc(Archive::getId);

        Page<Archive> pg = new Page<>(page, size);
        IPage<Archive> res = archiveMapper.selectPage(pg, qw);
        IPage<ArchiveVO> voPage = res.convert(ArchiveVO::fromEntity);
        return Result.success(voPage);
    }

    @Override
    public Result<ArchiveVO> detail(Long id) {
        if (id == null) {
            return Result.fail("未指定档案编号。");
        }
        Archive a = archiveMapper.selectById(id);
        if (a == null || (a.getStatus() != null && a.getStatus() == 0)) {
            return Result.fail("该档案已消散于迷雾之中。");
        }
        return Result.success(ArchiveVO.fromEntity(a));
    }
}
