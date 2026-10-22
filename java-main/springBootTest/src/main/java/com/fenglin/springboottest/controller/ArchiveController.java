package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fenglin.springboottest.common.ArchiveVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 神域档案接口（首页展示 / 搜索 / 详情）
 * 该控制器位于 /api/** 之下，需携带有效访问令牌（由 AuthInterceptor 校验）。
 */
@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    /**
     * 首页档案列表 + 搜索。
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
}
