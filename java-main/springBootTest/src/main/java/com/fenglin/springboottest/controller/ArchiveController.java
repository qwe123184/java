package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fenglin.springboottest.common.ArchiveSaveRequest;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.service.ArchiveService;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 神域档案接口（首页展示 / 搜索 / 详情 / 后台管理）
 * 该控制器位于 /api/** 之下，需携带有效访问令牌（由 AuthInterceptor 校验）。
 * 后台写操作（新增/修改/删除）以及后台档案库列表另需 ADMIN 角色，由 userService.isAdmin 校验。
 */
@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    /**
     * 首页档案列表 + 搜索（仅展示中档案）。
     * keyword 关键词（可选），type 类型（可选，空或"全部"=不限），page/size 分页（均从 1 开始）。
     */
    @GetMapping("/list")
    public Result<IPage<ArchiveVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long size) {
        return archiveService.search(keyword, type, page, size);
    }

    /**
     * 档案详情（供首页"查阅"按钮调用）
     */
    @GetMapping("/{id}")
    public Result<ArchiveVO> detail(@PathVariable Long id) {
        return archiveService.detail(id);
    }

    /**
     * 后台档案库列表（含下架），仅 ADMIN 可访问。
     */
    @GetMapping("/admin/list")
    public Result<IPage<ArchiveVO>> adminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权查看档案库。");
        }
        return archiveService.listAll(keyword, type, status, page, size);
    }

    /**
     * 新增档案（后台管理），仅 ADMIN 可操作。
     */
    @PostMapping
    public Result<ArchiveVO> create(
            @Valid @RequestBody ArchiveSaveRequest req,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权新增档案。");
        }
        return archiveService.create(req);
    }

    /**
     * 修改档案（后台管理），仅 ADMIN 可操作。
     */
    @PutMapping("/{id}")
    public Result<ArchiveVO> update(
            @PathVariable Long id,
            @Valid @RequestBody ArchiveSaveRequest req,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权修改档案。");
        }
        return archiveService.update(id, req);
    }

    /**
     * 删除档案（后台管理），仅 ADMIN 可操作。
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权删除档案。");
        }
        return archiveService.remove(id);
    }
}
