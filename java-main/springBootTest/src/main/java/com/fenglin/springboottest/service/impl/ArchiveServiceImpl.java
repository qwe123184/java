package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.ArchiveSaveRequest;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Archive;
import com.fenglin.springboottest.mapper.ArchiveMapper;
import com.fenglin.springboottest.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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

    @Override
    public Result<IPage<ArchiveVO>> listAll(String keyword, String type, Integer status, long page, long size) {
        LambdaQueryWrapper<Archive> qw = new LambdaQueryWrapper<>();

        if (status != null) {
            qw.eq(Archive::getStatus, status);
        }
        if (StringUtils.hasText(type) && !ALL.equals(type.trim())) {
            qw.eq(Archive::getType, type.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            qw.and(w -> w.like(Archive::getName, k)
                    .or().like(Archive::getDescription, k)
                    .or().like(Archive::getRealm, k)
                    .or().like(Archive::getType, k));
        }

        qw.orderByDesc(Archive::getSort).orderByDesc(Archive::getId);

        Page<Archive> pg = new Page<>(page, size);
        IPage<Archive> res = archiveMapper.selectPage(pg, qw);
        IPage<ArchiveVO> voPage = res.convert(ArchiveVO::fromEntity);
        return Result.success(voPage);
    }

    @Override
    public Result<ArchiveVO> create(ArchiveSaveRequest req) {
        if (req == null) {
            return Result.fail("档案内容不可为空。");
        }
        Archive a = new Archive();
        a.setName(req.getName().trim());
        a.setType(req.getType().trim());
        a.setRealm(StringUtils.hasText(req.getRealm()) ? req.getRealm().trim() : "");
        a.setDescription(StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : "");
        a.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        a.setSort(req.getSort() != null ? req.getSort() : 0);
        a.setCreateTime(LocalDateTime.now());
        archiveMapper.insert(a);
        return Result.success("档案「" + a.getName() + "」已铭刻于神域。", ArchiveVO.fromEntity(a));
    }

    @Override
    public Result<ArchiveVO> update(Long id, ArchiveSaveRequest req) {
        if (id == null || req == null) {
            return Result.fail("未指定档案编号或内容为空。");
        }
        Archive a = archiveMapper.selectById(id);
        if (a == null) {
            return Result.fail("该档案已消散于迷雾之中。");
        }
        a.setName(req.getName().trim());
        a.setType(req.getType().trim());
        a.setRealm(StringUtils.hasText(req.getRealm()) ? req.getRealm().trim() : "");
        a.setDescription(StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : "");
        if (req.getStatus() != null) {
            a.setStatus(req.getStatus());
        }
        if (req.getSort() != null) {
            a.setSort(req.getSort());
        }
        archiveMapper.updateById(a);
        return Result.success("档案「" + a.getName() + "」已更新。", ArchiveVO.fromEntity(a));
    }

    @Override
    public Result<Void> remove(Long id) {
        if (id == null) {
            return Result.fail("未指定档案编号。");
        }
        Archive a = archiveMapper.selectById(id);
        if (a == null) {
            return Result.fail("该档案已消散于迷雾之中。");
        }
        archiveMapper.deleteById(id);
        return Result.success("档案「" + a.getName() + "」已从神域抹除。", (Void) null);
    }
}
